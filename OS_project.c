#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

#define true 1
#define false 0
#define BUF_SIZE 100
#define LIMIT 10

void parse_line(char line[], char str_arr[][LIMIT], int *count)
{
	int line_len = 0;	
	int k = 0, j = 0;
	line_len = strlen(line);
	//printf("length is %d\n", line_len);
	for(int i=0;i<line_len;i++){
		
		if(line[i] == ' ' || line[i] == '\r' || line[i] == '\t' || line[i] == '\n' || line[i] == '\0') 
		{
			str_arr[k][j] = '\0';
			k++;
			j=0;
			(*count)++;
			continue;
		}
		str_arr[k][j] = line[i];
		j++;		
	}
}

int main()
{
	int words_count = 0;
	char line[BUF_SIZE];
	char cmd[LIMIT][LIMIT];

	char buff[2048];
	int len =0, index = 0;
	int n, status,b_count = 0;
	FILE *fp, *pd;

	pid_t childPid, cpid_array[100], w;
	printf("User shell program to execute user commands.\n");
	printf("Enter your command not exceeding 30 characters..\n");
	
	while (true) {
		len =0;
		printf("user Shell >");
		if(fgets(line,sizeof(line),stdin) != NULL) {
			len = strlen(line);
			//printf("line = %s   len = %d\n",line,len);
			line[len-1] = '\0';
			len = strlen(line);
			//printf("line = %s   len = %d\n",line,len);
			if(strcmp(line,"exit") == 0) break;
			printf("%c %d\n", line[len-1], line[len-1]);
			parse_line(line,cmd,&words_count);
				
			if(line[len-1] == '&')
			{
				printf("User requested background service\n"); 
				childPid = fork();
				if(childPid == 0){
					/*This is child process*/
					execvp(cmd[0], (char **)cmd);
				} else if (childPid < 0){
					printf("fork() failed..\n");
				} else {
					cpid_array[index] = childPid;
					b_count++;
					/* This os parent process. Pid = child process pid. 
					int returnStatus;
					waitpid(childPid, &returnStatus,0);
					if(returnStatus == 0) {
						printf("Child process finished.\n");
					} else if(returnStatus == 1) {
						printf("Child process terminated with ERROR..\n");
					}*/
				}
				
			} else
			{ 
				words_count = 0;
				/*printf("number of words = %d\n", words_count);
				for(int i=0;i < words_count;i++)
				{
					printf("cmd[%d] = ",i);
					for(int j=0;cmd[i][j]!='\0';j++)
					{
						printf("%c",cmd[i][j]);
					}
	                                printf("\n");
				}*/
			
	 			pd = popen(line, "r");
			        if (!pd) return 1;
			        while (fgets(buff, sizeof(buff), pd))
				{
	                		printf("%s", buff);
	        		}
	        		pclose(pd);
				/* Now check for any child processes running in the
				   background is finished. Give the status back */
				if(b_count != 0)
				{
					for (int j=0;j<100;j++) 
					{
						printf("Checking status of %d\n", j);
						w = waitpid(cpid_array[j], &status, WNOHANG);
						if(w == -1) printf("waitpid error\n");
					
						if (WIFEXITED(status)) {
							printf("[%d ==> done, status=%d\n",cpid_array[j], WEXITSTATUS(status));
						} else if (WIFSIGNALED(status)) {
							printf("[%d ==> done. status=%d\n",cpid_array[j], WTERMSIG(status));
						} else if (WIFSTOPPED(status)) {
							printf("[%d ==> done. status=%d\n",cpid_array[j], WSTOPSIG(status));
						}
						
					}
				}
			}
			
		}
	}
}


