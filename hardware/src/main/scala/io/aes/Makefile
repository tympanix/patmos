#
# Building Chisel examples without too much sbt/scala/... stuff
#
# sbt looks for default into a folder ./project and . for build.sdt and Build.scala
# sbt creates per default a ./target folder

SBT = sbt


# Generate Verilog code
aes:
	$(SBT) "runMain aes.AESMain"

sbox:
	$(SBT) "runMain aes.SBoxMain"

mixcolumns:
	$(SBT) "runMain aes.MixColumnsMain"

shiftrows:
	$(SBT) "runMain aes.ShiftRowsMain"

keyschedule:
	$(SBT) "runMain aes.KeyScheduleMain"

# Generate the C++ simulation and run the tests
aes-test:
	$(SBT) "test:runMain aes.AESTester"

sbox-test:
	$(SBT) "test:runMain aes.SBoxTester"

mixcolumns-test:
	$(SBT) "test:runMain aes.MixColumnsTester"

shiftrow-test:
	$(SBT) "test:runMain aes.ShiftRowsTester"

keyschedule-test:
	$(SBT) "test:runMain aes.KeyScheduleTester"
