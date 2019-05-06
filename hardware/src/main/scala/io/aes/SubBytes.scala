package io
import Chisel._

class SubBytes extends Module with SBoxValues {
  val io = IO(new Bundle {
    val in = Input(Vec(16, UInt(width = 8)))
    val out = Output(Vec(16, UInt(width = 8)))
  })

  io.out := io.in.map(v => SBOX(v))

}

// Generate the Verilog code by invoking the Driver
object SubBytesMain extends App {
  println("Generating the AES hardware")
  chiselMain(Array("--targetDir", "generated"), () => Module(new SubBytes()))
}