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
	return ((int)input[3] << 24) | ((int)input[2] << 16) | ((int)input[1] << 8) | (int)input[0];
}

int main(int argc, char **argv) {

	// Block to be used for AES 128 encryption
	unsigned char plain_text[16] = {0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6};

	// Encryption key
	unsigned char key[16] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

	// Encryption result
	unsigned char cipher_text[16];

	volatile _IODEV int *io_base_ptr = (volatile _IODEV int *) 0xf00b0000;
	volatile _IODEV int *io_timer_ptr = (volatile _IODEV int *) PATMOS_IO_TIMER;
	
	int start, end;


	// Write key input to mem
	for (int i = 0; i < 4; i++) {
		io_base_ptr[KEY_OFFSET + i] = 0x0;
	}

	// Begin timer 
	start = io_timer_ptr[1];
	
	// Write block input to mem
	for (int i = 0; i < 4; i++) {
		io_base_ptr[BLOCK_IN_OFFSET + i] = word(plain_text+(i*4));
	}


	// Write the configuration
	io_base_ptr[CONF_START] = 1;
	
	// Retrieve AES result
	for (int i = 0; i < 4; i++) {
		cipher_text[i*4] = io_base_ptr[BLOCK_OUT_OFFSET + i];
	}

	// End timer
	end = io_timer_ptr[1];

	printf("Clock cycles spent on AES: %d\n", end-start);
	
	for (int i = 0; i < 4; i++) {
		printf("%x\n", io_base_ptr[BLOCK_OUT_OFFSET + i]);
	}

  	return 0;
}
