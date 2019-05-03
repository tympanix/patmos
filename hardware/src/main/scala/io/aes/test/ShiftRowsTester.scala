package io

import Chisel._

/**
  * Test the ShiftRows module
  */
class ShiftRowsTester(dut: ShiftRows) extends Tester(dut) {

  val inputBlock = List(0x0d, 0x1b, 0x0c, 0xce, 0xeb, 0xb4, 0xcc, 0xf4, 0xbd, 0x4c, 0x2e, 0x0f, 0x30, 0x89, 0x8f, 0x8e)
  val outputBlock = List(0x0d, 0xb4, 0x2e, 0x8e, 0xeb, 0x4c, 0x8f, 0xce, 0xbd, 0x89, 0x0c, 0xf4, 0x30, 0x1b, 0xcc, 0x0f)

  // Feed a block (16 bytes) to the input wire
  for (i <- 0 until 16) {
    poke(dut.io.bin(i), inputBlock(i))
  }
  step(1)

  for(i <- 0 until 16) {
    expect(dut.io.bout(i), outputBlock(i))
  }
}

object ShiftRowsTester {
  println("Testing the Shift Rows module")
  def main(args: Array[String]): Unit = {
    chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new ShiftRows())) {
      c => new ShiftRowsTester(c)
    }
  }
}
