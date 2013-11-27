/* Source code van Voorbeeld 1A */

#include "vxWorks.h"
#include "stdio.h"
#include "stdlib.h"
#include "taskLib.h"
#include "sysLib.h"
#include "logLib.h"
#include "kernelLib.h"

void taskOne(void);
void taskTwo(void);
void taskThree(void);
void taskFour(void);

//27000@145.92.17.99 = host

#define LONG_TIME 100000

int taskIdOne, taskIdTwo, taskIdThree, taskIdFour;

STATUS start(void) {
	if (kernelTimeSlice(10) != OK)
		printf("Time slice error");

	if ((taskIdOne = taskSpawn("t1", 110, 0x100, 2000, (FUNCPTR) taskOne, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0)) == ERROR)
		printf("taskspawn taak 1 error");
	if ((taskIdTwo = taskSpawn("t2", 110, 0x100, 2000, (FUNCPTR) taskTwo, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0)) == ERROR)
		printf("taskspawn taak 2 error");
	if ((taskIdThree = taskSpawn("t3", 101, 0x100, 2000, (FUNCPTR) taskThree,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) == ERROR)
		printf("taskspawn taak 3 error");
	if ((taskIdFour = taskSpawn("t4", 101, 0x100, 2000, (FUNCPTR) taskFour, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0)) == ERROR)
		printf("taskspawn taak 4 error");

	return (OK);

}

void taskOne(void) {

	int i, j, y, m, n;

	while (1) {
		for (i = 0; i < 5; i++) {
			y = 1;
			for (j = 1; j <= 8; j++) {
				logMsg("taak 1 %i\n", y, 0, 0, 0, 0, 0);
				for (m = 0; m <= 1000; m++)
					for (n = 0; n <= LONG_TIME; n++)
						;
				y = y * 2;
			}
		}

	}

}

void taskTwo(void) {

	int i, j, y, m, n;

	while (1) {
		for (i = 0; i < 5; i++) {
			y = 1;
			for (j = 1; j <= 8; j++) {
				logMsg("taak 2 %i\n", y, 0, 0, 0, 0, 0);
				for (m = 0; m <= 1000; m++)
					for (n = 0; n <= LONG_TIME; n++)
						;
				y = y * 2;
			}
		}
		taskLock();
		taskPrioritySet(taskIdThree, 101);
		taskPrioritySet(taskIdFour, 101);
		taskUnlock();
	}
}

void taskThree(void) {
	int i, j, y, m, n;
	while (1) {
		for (i = 0; i < 5; i++) {
			y = 1;
			for (j = 1; j <= 8; j++) {
				logMsg("taak 3 %i\n", y, 0, 0, 0, 0, 0);
				for (m = 0; m <= 1000; m++)
					for (n = 0; n <= LONG_TIME; n++)
						;
				y = y * 2;
			}
		}
	}
}

void taskFour(void) {
	int i, j, y, m, n;
	while (1) {
		for (i = 0; i < 5; i++) {
			y = 1;
			for (j = 1; j <= 8; j++) {
				logMsg("taak 4 %i\n", y, 0, 0, 0, 0, 0);
				for (m = 0; m <= 1000; m++)
					for (n = 0; n <= LONG_TIME; n++)
						;
				y = y * 2;
			}
		}
		taskLock();
		taskPrioritySet(taskIdThree, 111);
		taskPrioritySet(taskIdFour, 111);
		taskUnlock();
	}
}

void stop(void) {
	taskDelete(taskIdOne);
	taskDelete(taskIdTwo);
	taskDelete(taskIdThree);
	taskDelete(taskIdFour);

	return;
}
