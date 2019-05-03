package io

import Chisel._

import ocp._

object Aes extends DeviceObject {

  def init(params: Map[String, String]) = {}

  def create(params: Map[String, String]) : Aes = {
    Module(new Aes())
  }

  trait Pins {}
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
  val localAddr = Bits(width = 16)
  localAddr := masterReg.Addr(15,0)

  val key = Mem(UInt(width = 8), 32)
  val blockIn = Mem(UInt(width = 8), 16)
  val blockOut = Mem(UInt(width = 8), 16)
  
  val contentReg = Reg(init = UInt(42, width = DATA_WIDTH))

  // Default OCP response
  io.ocp.S.Resp := OcpResp.NULL
  io.ocp.S.Data := Bits(0, width = DATA_WIDTH)

  // Handle OCP reads
  when (masterReg.Cmd === OcpCmd.RD) {
    io.ocp.S.Resp := OcpResp.DVA
    io.ocp.S.Data := contentReg + 5.U
  }

  // Handle OCP writes
  when (masterReg.Cmd === OcpCmd.WR) {
    io.ocp.S.Resp := OcpResp.DVA
    contentReg := masterReg.Data

    // Write to aes input block
    when(localAddr === AesAddr.BLOCK_IN) {
      val local = masterReg.Addr - AesAddr.BLOCK_IN
      for (i <- 0 until masterReg.ByteEn.getWidth) {
        val addr = local + UInt(i)
        when (masterReg.ByteEn(i) === Bits(1)) {
          blockIn(addr) := masterReg.Data(8*i+7, 8*i)
        }
      }
    }
  }
}