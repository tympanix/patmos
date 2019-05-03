package io

import Chisel._

/*class KeyScheduleWrapper extends Module {
  val sbox = Module(new SBox)
  val ksch = Module(new KeySchedule)

  val io = IO(new KeyScheduleIO)

/*   val io = IO(new Bundle {
    val keyIn = Input(Vec(16, UInt(8.W)))
    val iteration = Input(UInt(4.W))
    val validIn = Input(Bool())
    val roundKeyOut = Output(Vec(16, UInt(8.W)))
    val validOut = Output(Bool())
  })
*/
  io <> ksch.io
  sbox.io <> ksch.io.sbox
}*/

class KeyScheduleTester(dut: KeySchedule) extends Tester(dut) {
/*
  val testKeys = List(
    List(0x54, 0x68, 0x61, 0x74, 0x73, 0x20, 0x6D, 0x79, 0x20, 0x4B, 0x75, 0x6E, 0x67, 0x20, 0x46, 0x75),
    List(0xE2, 0x32, 0xFC, 0xF1, 0x91, 0x12, 0x91, 0x88, 0xB1, 0x59, 0xE4, 0xE6, 0xD6, 0x79, 0xA2, 0x93),
    List(0x56, 0x08, 0x20, 0x07, 0xC7, 0x1A, 0xB1, 0x8F, 0x76, 0x43, 0x55, 0x69, 0xA0, 0x3A, 0xF7, 0xFA),
    List(0xD2, 0x60, 0x0D, 0xE7, 0x15, 0x7A, 0xBC, 0x68, 0x63, 0x39, 0xE9, 0x01, 0xC3, 0x03, 0x1E, 0xFB),
    List(0xA1, 0x12, 0x02, 0xC9, 0xB4, 0x68, 0xBE, 0xA1, 0xD7, 0x51, 0x57, 0xA0, 0x14, 0x52, 0x49, 0x5B),
    List(0xB1, 0x29, 0x3B, 0x33, 0x05, 0x41, 0x85, 0x92, 0xD2, 0x10, 0xD2, 0x32, 0xC6, 0x42, 0x9B, 0x69),
    List(0xBD, 0x3D, 0xC2, 0x87, 0xB8, 0x7C, 0x47, 0x15, 0x6A, 0x6C, 0x95, 0x27, 0xAC, 0x2E, 0x0E, 0x4E),
    List(0xCC, 0x96, 0xED, 0x16, 0x74, 0xEA, 0xAA, 0x03, 0x1E, 0x86, 0x3F, 0x24, 0xB2, 0xA8, 0x31, 0x6A),
    List(0x8E, 0x51, 0xEF, 0x21, 0xFA, 0xBB, 0x45, 0x22, 0xE4, 0x3D, 0x7A, 0x06, 0x56, 0x95, 0x4B, 0x6C),
    List(0xBF, 0xE2, 0xBF, 0x90, 0x45, 0x59, 0xFA, 0xB2, 0xA1, 0x64, 0x80, 0xB4, 0xF7, 0xF1, 0xCB, 0xD8),
    List(0x28, 0xFD, 0xDE, 0xF8, 0x6D, 0xA4, 0x24, 0x4A, 0xCC, 0xC0, 0xA4, 0xFE, 0x3B, 0x31, 0x6F, 0x26)
)
*/
  val testKeys = List(
    List(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
    List(0x62, 0x63, 0x63, 0x63, 0x62, 0x63, 0x63, 0x63, 0x62, 0x63, 0x63, 0x63, 0x62, 0x63, 0x63, 0x63),
    List(0x9b, 0x98, 0x98, 0xc9, 0xf9, 0xfb, 0xfb, 0xaa, 0x9b, 0x98, 0x98, 0xc9, 0xf9, 0xfb, 0xfb, 0xaa),
    List(0x90, 0x97, 0x34, 0x50, 0x69, 0x6c, 0xcf, 0xfa, 0xf2, 0xf4, 0x57, 0x33, 0x0b, 0x0f, 0xac, 0x99),
    List(0xee, 0x06, 0xda, 0x7b, 0x87, 0x6a, 0x15, 0x81, 0x75, 0x9e, 0x42, 0xb2, 0x7e, 0x91, 0xee, 0x2b),
    List(0x7f, 0x2e, 0x2b, 0x88, 0xf8, 0x44, 0x3e, 0x09, 0x8d, 0xda, 0x7c, 0xbb, 0xf3, 0x4b, 0x92, 0x90),
    List(0xec, 0x61, 0x4b, 0x85, 0x14, 0x25, 0x75, 0x8c, 0x99, 0xff, 0x09, 0x37, 0x6a, 0xb4, 0x9b, 0xa7),
    List(0x21, 0x75, 0x17, 0x87, 0x35, 0x50, 0x62, 0x0b, 0xac, 0xaf, 0x6b, 0x3c, 0xc6, 0x1b, 0xf0, 0x9b),
    List(0x0e, 0xf9, 0x03, 0x33, 0x3b, 0xa9, 0x61, 0x38, 0x97, 0x06, 0x0a, 0x04, 0x51, 0x1d, 0xfa, 0x9f),
    List(0xb1, 0xd4, 0xd8, 0xe2, 0x8a, 0x7d, 0xb9, 0xda, 0x1d, 0x7b, 0xb3, 0xde, 0x4c, 0x66, 0x49, 0x41),
    List(0xb4, 0xef, 0x5b, 0xcb, 0x3e, 0x92, 0xe2, 0x11, 0x23, 0xe9, 0x51, 0xcf, 0x6f, 0x8f, 0x18, 0x8e)
  )
  
  poke(dut.io.validIn, false)

  // Go through the remaining keys
  for (j <- 0 to 9) {
    
    step(1)

    poke(dut.io.iteration, j)
    
    for (i <- 0 to 15) {
      poke(dut.io.keyIn(i), testKeys(j)(i))
    }
    
    poke(dut.io.validIn, true)
    
    step(1)

    poke(dut.io.validIn, false)

    while (peek(dut.io.validOut) == 0) {
      step(1)
    }

    for (i <- 0 to 15) {
      expect(dut.io.roundKeyOut(i), testKeys(j+1)(i))
    }

    expect(dut.io.validOut, true)
  }

}

object KeyScheduleTester {
  println("Testing the Key Schedule")
  def main(args: Array[String]): Unit = {
    chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new KeySchedule())) {
      c => new KeyScheduleTester(c)
    }
  }
}
