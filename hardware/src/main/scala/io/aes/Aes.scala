package io

import Chisel._

import ocp._

object Aes extends DeviceObject {

  def init(params: Map[String, String]) = {}

  def create(params: Map[String, String]) : Aes = {
    Module(new Aes())
  }

  trait Pins {
  }
}

// Constants for aes key lengths
object AesKey {
  val L128 = Bits("b00")
  val L192 = Bits("b01")
  val L256 = Bits("b10")
}

// Constants for aes mode of operation
object AesMode {
  val ECB = UInt(0, width = 8)
}

// Constants for aes local address space
object AesAddr {
  val CONF       = Bits("h0000")
  val START      = Bits("h0004")
  val KEY        = Bits("h1000")
  val BLOCK_IN   = Bits("h2000")
  val BLOCK_OUT  = Bits("h3000")
}

class Aes() extends CoreDevice() {

  override val io = new CoreDeviceIO() with Aes.Pins

  // Constants
  val DATA_WIDTH = 32
  
  // Internal modules
  val aesCore = Module(new AesCore)

  val stall = Bool()
  stall := false.B

  // Register for requests from OCP master
  val masterReg = Reg(io.ocp.M)
  masterReg := Mux(stall, masterReg, io.ocp.M)
  
  // Local address for core device
  val localAddr = Wire(Bits(width = 16))
  localAddr := masterReg.Addr(15,0)

  val blockInMask = (localAddr(15,12) === AesAddr.BLOCK_IN(15,12))
  val keyMask = (localAddr(15,12) === AesAddr.KEY(15,12))
  val blockOutMask = (localAddr(15,12) === AesAddr.BLOCK_OUT(15,12))

  // Registers
  val key = Mem(UInt(width = 8), 32)
  val blockIn = Mem(UInt(width = 8), 16)
  val blockOut = Mem(UInt(width = 8), 16)
  val busy = Reg(Bool(), init = false.B)
  
  val contentReg = Reg(init = UInt(42, width = DATA_WIDTH))

  // Default signals
  aesCore.io.keyIn := key
  aesCore.io.blockIn := blockIn
  aesCore.io.validIn := false.B
  aesCore.io.readyIn := true.B
  aesCore.io.mode := AesMode.ECB
  aesCore.io.keyLength := AesKey.L128

  // Default OCP response
  io.ocp.S.Resp := OcpResp.NULL
  io.ocp.S.Data := Bits(0, width = DATA_WIDTH)
  
  // Wait for computation to finish
  when (aesCore.io.validOut) {
    busy := false.B
  }

  // Handle OCP reads
  when (masterReg.Cmd === OcpCmd.RD) {
    io.ocp.S.Data := contentReg + 5.U

    // Read aes block result
    when (blockOutMask) {
      stall := true.B
      val offset = localAddr(7,0)
      when (!busy) {
        stall := false.B
        io.ocp.S.Resp := OcpResp.DVA
        io.ocp.S.Data := Cat(
          aesCore.io.blockOut(offset+3.U),
          aesCore.io.blockOut(offset+2.U),
          aesCore.io.blockOut(offset+1.U),
          aesCore.io.blockOut(offset+0.U)
        )
      }
    }
  }

  // Handle OCP writes
  when (masterReg.Cmd === OcpCmd.WR) {
    io.ocp.S.Resp := OcpResp.DVA
    contentReg := masterReg.Data

    // Write to aes input block
    when(blockInMask) {
      for (i <- 0 until masterReg.ByteEn.getWidth) {
        when (masterReg.ByteEn(i) === Bits(1)) {
          blockIn(localAddr(7,0) + i.U) := masterReg.Data(8*i+7, 8*i)
        }
      }
    }

    // Write to aes key
    when(keyMask) {
      for (i <- 0 until masterReg.ByteEn.getWidth) {
        when (masterReg.ByteEn(i) === Bits(1)) {
          key(localAddr(7,0) + i.U) := masterReg.Data(8*i+7, 8*i)
        }
      }
    }

    // Start computation
    when (localAddr === AesAddr.START) {
      stall := true.B
      aesCore.io.validIn := true.B

      when (aesCore.io.readyOut) {
        stall := false.B
        io.ocp.S.Resp := OcpResp.DVA
        busy := true.B
      }
    }
  }
}