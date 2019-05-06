package io
import Chisel._

class KeyScheduleIO extends Bundle {
  val keyIn = Input(Vec(32, UInt(width = 8)))
  val iteration = Input(UInt(width = 4))
  val validIn = Input(Bool())
  val roundKeyOut = Output(Vec(32, UInt(width = 8)))
  val validOut = Output(Bool())
}

/**
  * The Key Schedule module.
  * Note that this module calculates a single round of the key schedule, 
  * thus it has to be given a key to expand on and an iteration value.
  *
  */
class KeySchedule extends Module with SBoxValues {
  val io = IO(new KeyScheduleIO)
  
  val RCON_VALUES = List(0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36) 
  val RCON = Reg(init = Vec(RCON_VALUES.map(v => v.U)))

  // Default signals values
  io.validOut := true.B

  val t = Wire(Vec(4, UInt(width = 8)))

  t(0) := SBOX(io.keyIn(13)) ^ RCON(io.iteration)
  t(1) := SBOX(io.keyIn(14))
  t(2) := SBOX(io.keyIn(15))
  t(3) := SBOX(io.keyIn(12))

  // Xor t with four bytes of the previous key. This will be the first 4 bytes in roundKey.
  val w1 = Wire(Vec(4, UInt(width = 8)))
  for (i <- 0 to 3) {
    w1(i) := t(i) ^ io.keyIn(i)
    io.roundKeyOut(i) := w1(i)
  }

  // For the next 12 bytes of the round key; Xor the previous key (4 bytes at a time) with the latest created 4 bytes for the round key.
  val w2 = Wire(Vec(4, UInt(width = 8)))
  for (i <- 0 to 3) {
    w2(i) := io.keyIn(i+4) ^ w1(i)
    io.roundKeyOut(i+4) := w2(i)
  }

  val w3 = Wire(Vec(4, UInt(width = 8)))
  for (i <- 0 to 3) {
    w3(i) := io.keyIn(i+8) ^ w2(i)
    io.roundKeyOut(i+8) := w3(i)
  }

  val w4 = Wire(Vec(4, UInt(width = 8)))
  for (i <- 0 to 3) {
    w4(i) := io.keyIn(i+12) ^ w3(i)
    io.roundKeyOut(i+12) := w4(i)
  }
}

// Generate the Verilog code by invoking the Driver
object KeyScheduleMain extends App {
  println("Generating the AES hardware")
  chiselMain(Array("--targetDir", "generated"), () => Module(new KeySchedule()))
}
