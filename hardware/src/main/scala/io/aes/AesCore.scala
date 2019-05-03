package io

import Chisel._

/* AesCore is the hardware module reponsible for receiving the input key and block
 * and performing 10, 12 or 14 rounds depending on the key length.
 * 
 * The key length is either 128, 192 or 256 bits and is set by the "keyLength" input.
 * 
 * The block operation mode is set by the "mode" input.
 */

class AesCore extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(Vec(32, UInt(width = 8)))
    val blockIn = Input(Vec(16, UInt(width = 8)))
    val start = Input(Bool())
    val mode = Input(Bits(width = 2))
    val keyLength = Input(Bits(width = 2))
    val blockOut = Output(Vec(16, UInt(width = 8)))
  })

  // Modules
  val aesRoundModule = Module(new AesRound)
  val lastRound = MuxCase(10, Array(
    io.keyLength === AesKey.L128 -> 10.U,
    io.keyLength === AesKey.L192 -> 12.U, 
    io.keyLength === AesKey.L256 -> 14.U
  ))

  // Registers
  val roundNum = Reg(init = UInt(0, 4))

  val isLast = roundNum === lastRound

  // Registers and wires for maintaining state
  val sWait :: sRoundGive :: sRoundRecieve:: sFinish :: Nil = Enum(UInt(), 4)
  val state = Reg(init = sWait)
  
  // Default signals

  // Default register assignments
  state := state

  switch(state) {
    // Wait until start signal is high
    is (sWait) {
      when (io.start) {
        aesRoundModule.io.blockIn := io.blockIn
        aesRoundModule.io.keyIn := io.keyIn // io.keyIn is currently 32 bits to make room for any sized key of the 3. What to do?
        aesRoundModule.io.iterIn := roundNum
        state := sRoundGive
      } .otherwise {
        state := sWait
      }
    } 

    // When the aes round is ready to take input
    is (sRoundGive) {
      aesRoundModule.io.validIn := true.B
      
      when (aesRoundModule.io.readyIn) {
        state := sRoundRecieve 
      } .otherwise {
        state := sRoundGive
      }
    }

    // Wait for the aes round to give back a round result
    is (sRoundRecieve) {
      when (aesRoundModule.io.validOut) {
        when (isLast) {
          state := sFinish
        } .otherwise {
          aesRoundModule.io.blockIn := aesRoundModule.io.blockOut
          aesRoundModule.io.keyIn := aesRoundModule.io.keyOut
          roundNum := roundNum + 1.U
          aesRoundModule.io.iterIn := roundNum
        }
      } .otherwise {
        state := sRoundRecieve
      }
    }

    // All rounds have been ran, output the encryption block
    is (sFinish) {
      io.blockOut := aesRoundModule.io.blockOut
    }
  }
}

// Generate the Verilog code by invoking the Driver
object AesCoreMain extends App {
  println("Generating the AesCore hardware")
  chiselMain(Array("--targetDir", "generated"), () => Module(new AesCore()))
}