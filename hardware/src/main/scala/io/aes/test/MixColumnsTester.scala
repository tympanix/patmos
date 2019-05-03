package aes

import Chisel._

class MixColumnsTester(dut: MixColumns) extends Tester(dut) {

  val data = List(
    (List(0xdb, 0x13, 0x53, 0x45), List(0x8e, 0x4d, 0xa1, 0xbc)),
    (List(0xf2, 0x0a, 0x22, 0x5c), List(0x9f, 0xdc, 0x58, 0x9d)),
    (List(0x01, 0x01, 0x01, 0x01), List(0x01, 0x01, 0x01, 0x01)),
    (List(0xc6, 0xc6, 0xc6, 0xc6), List(0xc6, 0xc6, 0xc6, 0xc6)),
    (List(0xd4, 0xd4, 0xd4, 0xd5), List(0xd5, 0xd5, 0xd7, 0xd6)),
    (List(0x2d, 0x26, 0x31, 0x4c), List(0x4d, 0x7e, 0xbd, 0xf8))
  )

  data.foreach({ case (input, result) => performTest(input, result)})

  def performTest(input: List[Int], result: List[Int]) = {

    for (i <- 0 until input.length) {
      poke(dut.io.in(i), input(i))
    }

    for (i <- 0 until result.length) {
      expect(dut.io.out(i), result(i))
    }

    step(1)

  }

}

object MixColumnsTester {
  println("Testing the MixColumns")
  def main(args: Array[String]): Unit = {
    chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new MixColumns())) {
      c => new MixColumnsTester(c)
    }
  }
}