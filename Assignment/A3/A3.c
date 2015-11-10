#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <ctype.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int eval(char* ch);

int main(int argc, char *argv[]){
    if(argc > 2 || argc <2){
        fprintf(stderr, "ERROR: Invalid arguments\nUSAGE: ./a.out <input-file>\n");
		return EXIT_FAILURE;
	 }
	 FILE *file; 
     char name[200000];
     pid_t parentId = getpid();
     if((file = fopen(argv[1],"r")) == NULL) { 
         fprintf(stderr,"ERROR: Can't find the file.\n"); 
         return EXIT_FAILURE;
     } 
     while(fgets(name, sizeof(name)-1, file) != NULL){
        if (getpid() == parentId){
            int result = eval(name);
            if (getpid() == parentId){
            printf("PROCESS %d: Final answer is '%d'\n",getpid(), result);
          }
        }
     }
     fclose(file);                     
     return EXIT_SUCCESS;
}
int eval(char* ch){
      char **token = (char**) calloc(300, sizeof(char*));
      if(token==NULL){
		    fprintf(stderr,"Error in calloc.\n"); 
		    return EXIT_FAILURE;
      }
	  char *part;
	  char *unuse;
	  char temp[1024];
	  int count = 0;
	  strcpy(temp, ch);
	  token[count]= malloc((strlen(temp)+1)*sizeof(char));
	  part = strtok_r(temp, "()", &unuse);
      while(part!=NULL){
          strcpy(token[count], part);
          count++;
          token[count]=malloc((strlen(temp)+1)*sizeof(char));
          part = strtok_r(NULL, "()", &unuse);
      }
      char operat = token[0][0];
      char charr[30];
      strcpy(charr, token[0]);
      char *partM;
      char *unuseM;
      char tempM[1024];
      strcpy(tempM, charr);
      partM = strtok_r(tempM, " \t", &unuseM);
      if ((operat != '+') && (operat != '-') && (operat != '*') && (operat != '/')){
          fprintf(stderr, "PROCESS %d: unknown '%s' operator\n", getpid(), partM);
          return EXIT_FAILURE;
      }
      char **token1 = (char**)calloc(20, sizeof(char*));
      if(token1 == NULL){
        printf("Error in calloc.\n"); 
        return EXIT_FAILURE;
      }
	  int countTemp = 0;
	  int count1 = 0;
	  char *part1;
	  char *unuse1;
	  char temp_1[1024];
	  strcpy(temp_1, token[countTemp]);
      part1 = strtok_r(temp_1, "+-/* \t", &unuse1);
      token1[count1]=malloc((strlen(temp_1)+1)*sizeof(char));
      while(part1!=NULL){
          strcpy(token1[count1], part1);
          count1++;
          token1[count1]=malloc((strlen(temp_1)+1)*sizeof(char));
          part1 = strtok_r(NULL, " \t", &unuse1);
      }
      countTemp++;
      while(countTemp<count-1){
          if(!isspace(token[countTemp][0])){
            if(!isdigit(token[countTemp][0])){
                strcpy(token1[count1], token[countTemp]);
                count1++;
                token1[count1]=malloc((strlen(temp_1)+1)*sizeof(char));
            }
         }else{
             char* part2;
             char* unuse2;
             char temp_2[1024];
             strcpy(temp_2, token[countTemp]);
             part2 = strtok_r(temp_2, " \t", &unuse2);
             token1[count1]=malloc((strlen(temp_2)+1)*sizeof(char));
             while(part2!=NULL){
                strcpy(token1[count1], part2);
                count1++;
                token1[count1]=malloc((strlen(temp_2)+1)*sizeof(char));
                part2 = strtok_r(NULL, " \t", &unuse2);
             }
         }
         countTemp++;
       }
      printf("PROCESS %d: Starting '%c' operation\n", getpid(), operat);
      if (count1<2){
          fprintf(stderr, "PROCESS %d: ERROR: not enough operands\n", getpid());
          return EXIT_FAILURE;
      }
      int *number = (int*)calloc(count1, sizeof(int));
      int number_count = 0;
      int i;
      for(i = 0; i < count1; i++){
          int p[2];
          int rc = pipe( p );
          if ( rc == -1 ){
              fprintf(stderr, "pipe() failed" );
              return EXIT_FAILURE;
       }
       pid_t pid = fork();
       if ( pid == -1 ){
          fprintf(stderr, "fork() failed" );
          return EXIT_FAILURE;
       }
       if (pid == 0 ){
           close(p[0]);   
           p[0] = -1;
           pid_t curr_pid = getpid();
           if(isdigit(token1[i][0])){
             if(getpid() == curr_pid){
                 write(p[1], token1[i], strlen(token1[i]));
                 printf("PROCESS %d: Sending '%s' on pipe to parent\n", getpid(), token1[i]);
             }
          }else{
            int tpVal = eval(token1[i]);
            char tpStr[20];
            sprintf(tpStr, "%d", tpVal);
            if(getpid() == curr_pid){
              write(p[1], tpStr, strlen(tpStr));
              printf("PROCESS %d: Sending '%s' on pipe to parent\n", getpid(), tpStr);
            }
          }
          return EXIT_SUCCESS;
       }else{
          close(p[1]);
          int status;
          pid_t child_pid = wait( &status );
          if ( WIFSIGNALED( status ) ){
             printf( "Child %d terminated abnormally\n", child_pid );
          }else if ( WIFEXITED( status ) ){
              int rc = WEXITSTATUS( status );
              if(rc!=0){
              printf( "Child %d terminated with nonzero exit status", child_pid );
              }
          }
          char buffer[100];
          int byts = read(p[0], buffer, 10);   
          buffer[byts] = '\0';
          number[number_count] = atoi(buffer);
          number_count++;
        }
     }
     int index;
     int val = number[0];
     for (index = 1; index < number_count; index++){
       if (operat=='+'){
         val = val + number[index];
       }else if (operat=='-'){
         val = val - number[index];
       }else if (operat=='*'){
         val = val *number[index];
       }else if (operat=='/'){
         val = val / number[index];
       }
     }
     return val;
}