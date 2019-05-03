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
class KeySchedule extends Module {
  val io = IO(new KeyScheduleIO)
  
  val sbox = Module(new SBox())
  
  // TODO: Can we calculate RCON to save space?
  val RCON_VALUES = List(0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36) 
  val RCON = Reg(init = Vec(RCON_VALUES.map(v => v.U)))

  // Default signals values
  io.validOut := false.B
  // io.sbox <> DontCare

  /* Assign the value of the four bytes from the previous key and rotate them.
   * Rotate the 8 high bits to be the lower 8 bits in a circular rotate.
   * Assumes that a low index on the vector is MSB and high index is LSB.
   * Perform rcon_i operation on the left most byte.
   * Perform SBox operation on the four bytes in the word
  */
  val t = Reg(Vec(4, UInt(width = 8)))

  // Default signals
  sbox.io.in := io.keyIn(13)

  val sWait :: sSub1 :: sSub2 :: sSub3 :: sSub4 :: sOutput :: Nil = Enum(UInt(), 6)
  val state = Reg(init = sWait)

  switch(state) {
    is (sWait) {
      when (io.validIn) {
        state := sSub1
      }
    }

    is (sSub1) {
      sbox.io.in := io.keyIn(13) 
      t(0) := sbox.io.out ^ RCON(io.iteration)
      state := sSub2
    }

    is (sSub2) {
      sbox.io.in := io.keyIn(14)
      t(1) := sbox.io.out
      state := sSub3
    }

    is (sSub3) {
      sbox.io.in := io.keyIn(15)
      t(2) := sbox.io.out
      state := sSub4
    }

    is (sSub4) {
      sbox.io.in := io.keyIn(12)
      t(3) := sbox.io.out
      state := sOutput
    }
    
    is (sOutput) {
      io.validOut := true.B
      state := sWait
    }
  }

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
