#include <stdio.h>
#include <machine/patmos.h>

#define KEY_OFFSET 			(0x00001000 >> 2)
#define BLOCK_IN_OFFSET 	(0x00002000 >> 2)
#define BLOCK_OUT_OFFSET	(0x00003000 >> 2)
#define CONF_KEY_LENGTH		(0x00000100 >> 2)
#define CONF_MODE			(0x00000200 >> 2)
#define CONF_START			(0x00000004 >> 2)

// Take four words, which are in succession of eachother and return them as a 32 bit result
int word(unsigned char input[]) {
	return ((int)input << 24) | ((int)input[1] << 16) | ((int)input[2] << 8) | (int)input[3];
}

int main(int argc, char **argv) {

	// Block to be used for AES 128 encryption
	unsigned char plain_text[16] = {0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6};

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// Encryption key. Should we give 16 or 32 here? 32 is used internally in the hardware
	unsigned char key[16] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

	// Encryption result
	unsigned char cipher_text[16];

	volatile _IODEV int *io_base_ptr = (volatile _IODEV int *) 0xf00b0000;

	// Write key and block input to mem
	for (int i = 0; i < 4; i++) {
		io_base_ptr[BLOCK_IN_OFFSET + i] = 0x12345;
	}

	// Write the configuration
//	io_base_ptr[CONF_KEY_LENGTH] = 1;
//	io_base_ptr[CONF_MODE] = 0;
	io_base_ptr[CONF_START] = 1;
	
	printf("%x\n", io_base_ptr[BLOCK_OUT_OFFSET]);

  	return 0;
}
