package aes

import Chisel._

//object AES extends DeviceObject {
//
//  def init(params: Map[String, String]) = {}
//
//  def create(params: Map[String, String]) : AES = {
//    Module(new AES())
//  }
//
//  trait Pins {}
//}
//
//class AES() extends CoreDevice()


class AesRound extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(Vec(16, UInt(width = 8)))
    val blockIn = Input(Vec(16, UInt(width = 8)))
    val validIn = Input(Bool())
    val readyIn = Input(Bool())
    val iterIn = Input(UInt(width = 4))
    val keyOut = Output(Vec(16, UInt(width = 8)))
    val blockOut = Output(Vec(16, UInt(width = 8)))
    val validOut = Output(Bool())
  })

  val sBoxModule = Module(new SBox)
  val shiftRowsModule = Module(new ShiftRows)
  val mixColumnsModule = Module(new MixColumns)
  val keyScheduleModule = Module(new KeySchedule)

//  sBoxModule.io <> DontCare
//  shiftRowsModule.io <> DontCare
//  mixColumnsModule.io <> DontCare
//  keyScheduleModule.io <> DontCare
  
  // Registers and wires for maintaining state
  val sWait :: sSubBytes :: sShiftRows :: sMixColumns :: sAddRoundKey :: sKeySchedule :: sFinished :: Nil = Enum(UInt(), 7)
  val state = Reg(init = sWait)

  //val block = Reg(Vec(16, UInt(width = 8)))
  val block = Vec(16, Reg(init = Bits(8)))
  val roundKey = Reg(Vec(16, UInt(width = 8)))
  val count = Reg(init = UInt(0, 4))
  val iter = Reg(init = UInt(0, 4))

  def mkcol(i: Int): Vec[UInt] = {
    return Vec(block(i), block(i+4), block(i+8), block(i+12))
  }

  val col = Vec(mkcol(0), mkcol(1), mkcol(2), mkcol(3))

  // Default signals
  sBoxModule.io.in := block(0)
  shiftRowsModule.io.bin := block

  mixColumnsModule.io.in(0) := block(0)
  mixColumnsModule.io.in(1) := block(4)
  mixColumnsModule.io.in(2) := block(8)
  mixColumnsModule.io.in(3) := block(12)
  
  keyScheduleModule.io.keyIn := roundKey
  keyScheduleModule.io.iteration := iter
  keyScheduleModule.io.validIn := false.B

  // Static output signal assignments
  io.validOut := false.B
  io.keyOut := roundKey
  io.blockOut := block
  

  // Static register assignments
  state := state

  switch (state) {
    // Wait for valid input
    is (sWait) {
      when (io.validIn) {
        block := io.blockIn
        count := 0.U
        iter := io.iterIn
        roundKey := io.keyIn
        when (io.iterIn === 0.U) {
          state := sAddRoundKey
        } .otherwise {
          state := sSubBytes
        }
      }
    }

    // Perform sub bytes operation
    is (sSubBytes) {
      sBoxModule.io.in := block(count)
      block(count) := sBoxModule.io.out
      count := count + 1.U
      state := sSubBytes

      when (count === 15.U) {
        count := 0.U
        state := sShiftRows
      }
    }

    // Perform shift rows operation
    is (sShiftRows) {
      shiftRowsModule.io.bin := block
      block := shiftRowsModule.io.bout

      when (iter === 10.U) {
        state := sAddRoundKey
      } .otherwise {
        state := sMixColumns
      }
    }

    // Perform mix columns operation
    is (sMixColumns) {
      val col = Wire(mixColumnsModule.io.out)
      
      mixColumnsModule.io.in(0) := block(count)
      mixColumnsModule.io.in(1) := block(count+1.U)
      mixColumnsModule.io.in(2) := block(count+2.U)
      mixColumnsModule.io.in(3) := block(count+3.U)

      block(count) := mixColumnsModule.io.out(0)
      block(count+1.U) := mixColumnsModule.io.out(1)
      block(count+2.U) := mixColumnsModule.io.out(2)
      block(count+3.U) := mixColumnsModule.io.out(3)

      count := count + 4.U

      when (count >= 12.U) {
        count := 0.U
        state := sAddRoundKey
      } .otherwise {
        state := sMixColumns
      }
    }

    // Perform add round key operation
    is (sAddRoundKey) {
      for (i <- 0 until 16) {
        block(i) := block(i) ^ roundKey(i)
      }
      state := sKeySchedule
    }

    // Perform key schedule of the round key
    is (sKeySchedule) {
      keyScheduleModule.io.validIn := true.B
      state := sKeySchedule

      when (keyScheduleModule.io.validOut) {
        roundKey := keyScheduleModule.io.roundKeyOut
        state := sFinished
      }
    }

    // Result ready, wait for handshake
    is (sFinished) {
      io.validOut := true.B
      state := sFinished

      when (io.readyIn) {
        state := sWait
      }
    }
  }
}

// Generate the Verilog code by invoking the Driver
object AesRoundMain extends App {
  println("Generating the AES hardware")
  chiselMain(Array("--targetDir", "generated"), () => Module(new AesRound()))
}
