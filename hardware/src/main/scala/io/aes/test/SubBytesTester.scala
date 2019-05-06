package io

import Chisel._

class SubBytesTester(dut: SubBytes) extends Tester(dut) with SBoxValues {

  val testBlock = List(0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6)

  for (i <- 0 until 16) {
    poke(dut.io.in(i), testBlock(i))
  }

  step(1)

  for (i <- 0 until 16) {
    expect(dut.io.out(i), SBOX_VALUES(testBlock(i)))
  }
}

object SubBytesTester {
  println("Testing the SubBytes module")
  def main(args: Array[String]): Unit = {
    chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new SubBytes())) {
      c => new SubBytesTester(c)
    }
  }
}
