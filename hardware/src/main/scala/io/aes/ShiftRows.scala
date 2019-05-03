package io
import Chisel._

/**
  * Shift Rows operates on a 16 byte (4x4 byte) block, thus making each row 4 bytes.
  */
class ShiftRows extends Module {
  val io = IO(new Bundle {
    val bin = Input(Vec(16, UInt(width = 8)))
    val bout = Output(Vec(16, UInt(width = 8)))
  })

  // First row of the block is not touched
  io.bout(0) := io.bin(0)
  io.bout(4) := io.bin(4)
  io.bout(8) := io.bin(8)
  io.bout(12) := io.bin(12)

  // Second row shifted by offset 1
  io.bout(1) := io.bin(5)
  io.bout(5) := io.bin(9)
  io.bout(9) := io.bin(13)
  io.bout(13) := io.bin(1)

  // Third row shifted by offset 2
  io.bout(2) := io.bin(10)
  io.bout(6) := io.bin(14)
  io.bout(10) := io.bin(2)
  io.bout(14) := io.bin(6)

  // Fourth row shifted by offset 3
  io.bout(3) := io.bin(15)
  io.bout(7) := io.bin(3)
  io.bout(11) := io.bin(7)
  io.bout(15) := io.bin(11)
}

// Generate the Verilog code by invoking the Driver
object ShiftRowsMain extends App {
  println("Generating the AES hardware")
  chiselMain(Array("--targetDir", "generated"), () => Module(new ShiftRows()))
}