
package io

import Chisel._

import ocp._

class AesTester(dut: Aes) extends Tester(dut) {

  val TEST_VALUES = List(0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6)

  val testBlock = TEST_VALUES.map(v => UInt(v, width = 8))

  def read(value: BigInt, addr: BigInt): Unit = {
    poke(dut.io.ocp.M.Cmd, OcpCmd.RD.litValue())
    poke(dut.io.ocp.M.Addr, addr)
    while (peek(dut.io.ocp.S.Resp) != OcpResp.DVA.litValue()) {
      step(1)
    }
    expect(dut.io.ocp.S.Data, value)
  }

  def read(value: UInt, addr: UInt): Unit = {
    read(addr.litValue(), value.litValue())
  }
  
  def send(value: BigInt, addr: BigInt): Unit = {
    poke(dut.io.ocp.M.Cmd, OcpCmd.WR.litValue())
    poke(dut.io.ocp.M.Data, value)
    poke(dut.io.ocp.M.Addr, addr)
    poke(dut.io.ocp.M.ByteEn, Bits("b1111").litValue())
    step(1)
    while (peek(dut.io.ocp.S.Resp) != OcpResp.DVA.litValue()) {
      step(1)
    }
  }

  def send(value: UInt, addr: UInt): Unit = {
    send(value.litValue(), addr.litValue())
  }

  for (i <- 0 until 4) {
    val row = Cat(
      testBlock(i*4+3),
      testBlock(i*4+2),
      testBlock(i*4+1),
      testBlock(i*4)
    )
    send(row, AesAddr.BLOCK_IN + (i * 4).U)
  }
  
  step(1)
  
  for (i <- 0 until 16) {
    expect(dut.io.blockIn(i), testBlock(i).litValue())
  }

}

object AesTester {
  println("Testing the Aes Module")
    def main(args: Array[String]): Unit = {
      chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new Aes())) {
        c => new AesTester(c)
    }
  }
}
