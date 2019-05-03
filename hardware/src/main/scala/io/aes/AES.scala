package aes

import Chisel._

object AES extends DeviceObject {

  def init(params: Map[String, String]) = {}

  def create(params: Map[String, String]) : AES = {
    Module(new AES())
  }

  trait Pins {}
}

class AES() extends CoreDevice() {

  override val io = new CoreDeviceIO() with AES.Pins

  // Constants
  val DATA_WIDTH = 32

  // Register for requests from OCP master
  val masterReg = Reg(next = io.ocp.M)

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
  }
}