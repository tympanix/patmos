
package io

import Chisel._

import ocp._

class AesTester(dut: Aes) extends Tester(dut) {

  val BLOCK_VALUES = List(0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6)
  val KEY_VALUES = List(0xb4, 0xef, 0x5b, 0xcb, 0x3e, 0x92, 0xe2, 0x11, 0x23, 0xe9, 0x51, 0xcf, 0x6f, 0x8f, 0x18, 0x8e)

  val testBlock = BLOCK_VALUES.map(v => UInt(v, width = 8))
  val testKey = KEY_VALUES.map(v => UInt(v, width = 8))

  def read(value: BigInt, addr: BigInt): Unit = {
    poke(dut.io.ocp.M.Cmd, OcpCmd.RD.litValue())
    poke(dut.io.ocp.M.Addr, addr)
    step(1)
    while (peek(dut.io.ocp.S.Resp) != OcpResp.DVA.litValue()) {
      step(1)
    }
    expect(dut.io.ocp.S.Data, value)
  }

  def read(value: UInt, addr: UInt): Unit = {
    read(value.litValue(), addr.litValue())
  }
  
  def write(value: BigInt, addr: BigInt): Unit = {
    poke(dut.io.ocp.M.Cmd, OcpCmd.WR.litValue())
    poke(dut.io.ocp.M.Data, value)
    poke(dut.io.ocp.M.Addr, addr)
    poke(dut.io.ocp.M.ByteEn, Bits("b1111").litValue())
    step(1)
    while (peek(dut.io.ocp.S.Resp) != OcpResp.DVA.litValue()) {
      step(1)
    }
  }

  def write(value: UInt, addr: UInt): Unit = {
    write(value.litValue(), addr.litValue())
  }

  // // Write block into aes device
  // for (i <- 0 until 4) {
  //   val row = Cat(
  //     testBlock(i*4+3),
  //     testBlock(i*4+2),
  //     testBlock(i*4+1),
  //     testBlock(i*4)
  //   )
  //   send(row, AesAddr.BLOCK_IN + (i*4).U)
  // }
  // step(1)
  
  // for (i <- 0 until 16) {
  //   expect(dut.io.blockIn(i), testBlock(i).litValue())
  // }
  
  // // Write key into aes device
  // for (i <- 0 until 4) {
  //   val row = Cat(
  //     testKey(i*4+3),
  //     testKey(i*4+2),
  //     testKey(i*4+1),
  //     testKey(i*4)
  //   )
  //   send(row, AesAddr.KEY + (i*4).U)
  // }
  // step(1)

  // for (i <- 0 until 16) {
  //   expect(dut.io.key(i), testKey(i).litValue())
  // }
  
  // Start computation
  write(1.U, AesAddr.START)
  step(1)

  // Read out block from aes device
  for (i <- 0 until 4) {
    val row = Cat(
      UInt(i*4+4, width = 8),
      UInt(i*4+3, width = 8),
      UInt(i*4+2, width = 8),
      UInt(i*4+1, width = 8)
    )
    read(row, AesAddr.BLOCK_OUT + (i*4).U)
  }
  
  step(1)

}

object AesTester {
  println("Testing the Aes Module")
    def main(args: Array[String]): Unit = {
      chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new Aes())) {
        c => new AesTester(c)
    }
  }
}
