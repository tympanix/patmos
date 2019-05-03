package io

import Chisel._

import ocp._

object Aes extends DeviceObject {

  def init(params: Map[String, String]) = {}

  def create(params: Map[String, String]) : Aes = {
    Module(new Aes())
  }

  trait Pins {
    val blockIn = Output(Vec(16, UInt(width = 8)))
    val key = Output(Vec(32, UInt(width = 8)))
    val blockOut = Output(Vec(16, UInt(width = 8)))
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

  // Register for requests from OCP master
  val masterReg = Reg(next = io.ocp.M)
  
  // Local address for core device
  val localAddr = Wire(Bits(width = 16))
  localAddr := masterReg.Addr(15,0)

  val blockInMask = (localAddr(15,12) === AesAddr.BLOCK_IN(15,12))
  val keyMask = (localAddr(15,12) === AesAddr.KEY(15,12))
  val blockOutMask = (localAddr(15,12) === AesAddr.BLOCK_OUT(15,12))

  val key = Mem(UInt(width = 8), 32)
  val blockIn = Mem(UInt(width = 8), 16)
  val blockOut = Mem(UInt(width = 8), 16)
  
  val contentReg = Reg(init = UInt(42, width = DATA_WIDTH))

  // Default output
  io.blockIn := blockIn
  io.key := key
  io.blockOut := blockOut

  // Default OCP response
  io.ocp.S.Resp := OcpResp.NULL
  io.ocp.S.Data := Bits(0, width = DATA_WIDTH)

  // Handle OCP reads
  when (masterReg.Cmd === OcpCmd.RD) {
    io.ocp.S.Resp := OcpResp.DVA
    io.ocp.S.Data := contentReg + 5.U

    // Read aes block result
    when (blockOutMask) {
      io.ocp.S.Resp := OcpResp.DVA
      val offset = localAddr(7,0)
      io.ocp.S.Data := Cat(
        blockOut(offset+3.U),
        blockOut(offset+2.U),
        blockOut(offset+1.U),
        blockOut(offset+0.U)
      )
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
      io.ocp.S.Resp := OcpResp.DVA
      for (i <- 0 until 16) {
        blockOut(i) := (i+1).U
      }
    }
  }
}