
package io

import Chisel._

import ocp._

class AesTester(dut: Aes) extends Tester(dut) {

  def read(v: UInt): Unit = {
    read(v.litValue())
  }

  def read(v: BigInt): Unit = {
    poke(dut.io.ocp.M.Cmd, OcpCmd.RD.litValue())
    while (peek(dut.io.ocp.S.Resp) != OcpResp.DVA.litValue()) {
      step(1)
    }
    expect(dut.io.ocp.S.Data, v)
  }

  read(47.U)
  
}

object AesTester {
  println("Testing the Aes Module")
    def main(args: Array[String]): Unit = {
      chiselMainTest(Array[String]("--genHarness", "--test", "--backend", "c", "--compile", "--vcd", "--targetDir", "generated"), () => Module(new Aes())) {
        c => new AesTester(c)
    }
  }
}
