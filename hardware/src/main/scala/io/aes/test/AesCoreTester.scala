package io

import Chisel._

class AesCoreTester(dut: AesCore) extends Tester(dut) {

  val testKey = List(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

  val testBlockIn = List(0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6)
  val testBlockOut = List(0x03, 0x36, 0x76, 0x3e, 0x96, 0x6d, 0x92, 0x59, 0x5a, 0x56, 0x7c, 0xc9, 0xce, 0x53, 0x7f, 0x5e)


  // Give a key and a block of data and let the AesCore run all the rounds required

  for (i <- 0 to 31) {
    poke(dut.io.keyIn(i), testKey(i))
  }

  for (i <- 0 to 15) {
    poke(dut.io.blockIn(i), testBlockIn(i))
  }
  poke(dut.io.keyLength, AesKey.L128.litValue()) 
  poke(dut.io.mode, true)
  poke(dut.io.validIn, true)
  
  // Wait for handshake
  while (peek(dut.io.readyOut) == 0) {
    step(1)
  }
  
  step(1)
  poke(dut.io.validIn, false)

  // Wait for handshake
  while (peek(dut.io.validOut) == 0) {
    step(1)
  }

  for (i <- 0 to 15) {
    expect(dut.io.blockOut(i), testBlockOut(i))
  }
  
  poke(dut.io.readyIn, true)
  step(1)

}

object AesCoreTester {
  println("Testing the AesCore Module")
    def main(args: Array[String]): Unit = {
      chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new AesCore())) {
        c => new AesCoreTester(c)
    }
  }
}
