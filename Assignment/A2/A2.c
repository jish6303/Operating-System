#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/wait.h>

int main(int argc, char * argv[])
{
	if(argc > 3 || argc <3){
		perror("Error: Invalid arguments\nUSAGE: ./a.out <input-file> <chunk-size>\n");
         return EXIT_FAILURE;
	}
	char name[80];
	strcpy( name, argv[1]);
	int fd = open( name, O_RDONLY );

	if ( fd == -1 )
	{
		perror("open() failed. Most likely file not found.\n" );
		return EXIT_FAILURE;
	}
	struct stat st;
	lstat(argv[1], &st);
	long long size = st.st_size;
	printf("PARENT: File '%s' contains %lld bytes\n", argv[1], size);
	if (size <= 0){
		perror("Less than one bytes read :-(\n");
		return EXIT_FAILURE;
    }
	int capacity = atoi(argv[2]);
	if(capacity == 0){
		perror("number input wrong :-(\n");
		return EXIT_FAILURE;
	}
	if(capacity < 0){
		perror("PLease input a number greater than 0:-(\n");
		return EXIT_FAILURE;
	}
	int count = 1 + ((int)(size - 1))/capacity;
	printf("PARENT: ... and will be processed via %d child processes\n", count);
	int i;
	pid_t pid;
	for(i = 0; i < count; i++){
		pid = fork();
		if(pid == -1){
			perror("fork() failed\n");
			return EXIT_FAILURE;
		}
		if(pid == 0){
			printf( "CHILD %d CHUNK: ", getpid() );
			char buffer[capacity + 1];
			int rc = read( fd, buffer, capacity);
			if ( rc == -1 ){
			    perror( "read() failed" );
			    return EXIT_FAILURE;
			}
			buffer[rc] = '\0';
			printf( "%s\n", buffer );
			return 0;
		} else {
			int status;
		    pid_t child_pid = wait( &status );
		    if ( WIFSIGNALED( status ) ) 
		    {
		    	printf("PARENT: child <%d> terminated abnormally\n", (int)child_pid);
		    }
		    else if ( WIFEXITED( status ) )
		    {
		      int rc = WEXITSTATUS( status );
			  if(rc > 0){
				  printf("PARENT: child <%d> terminated with nonzero exit status <%d>\n", (int)child_pid, rc);
			  }
		    }
		}
	}
	return EXIT_SUCCESS;       
}
