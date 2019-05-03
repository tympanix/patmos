package aes

import Chisel._

/**
 * Rijndael MixColumns operation for AES implementation.
 * Operates of a single row (4 bytes) and performs the Galois field multiplication
 */
class MixColumns extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(4, UInt(width = 8)))
    val out = Output(Vec(4, UInt(width = 8)))
  })

  def xtime(x: UInt): UInt = {
    return ((x<<1) ^ Mux(x(x.getWidth - 1), 0.U, 0x1b.U))(7,0)
  }
  
  val c1 = Wire(init = io.in)
  val c2 = Wire(init = Vec(c1.map(v => xtime(v))))
  val c3 = Wire(init = Vec(c1.zip(c2).map(v => v._1 ^ v._2)))

  io.out(0) := c2(0) ^ c3(1) ^ c1(2) ^ c1(3) 
  io.out(1) := c1(0) ^ c2(1) ^ c3(2) ^ c1(3)
  io.out(2) := c1(0) ^ c1(1) ^ c2(2) ^ c3(3)
  io.out(3) := c3(0) ^ c1(1) ^ c1(2) ^ c2(3)

}

// Generate the Verilog code by invoking the Driver
object MixColumnsMain extends App {
  println("Generating the AES hardware")
  chiselMain(Array("--targetDir", "generated"), () => Module(new MixColumns()))
}