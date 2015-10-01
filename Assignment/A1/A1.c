#include "stdio.h"
#include "stdlib.h"
#include "string.h"
#include "stdbool.h"

#define INITIAL_LEN 20
bool startsWith(const char *a, const char *b)
{
   if(strncmp(a, b, strlen(b)) == 0) return 1;
   return 0;
}

int main(int argc, char * argv[])
{
	if(argc > 3){
		fprintf(stderr, "Argument number bigger than 3 :-(\n");
         return 1;
	}
	if(argc < 3){
		fprintf(stderr, "Argument number smaller than 3 :-(\n");
         return 1;
	}
    FILE *fp1;        
    char oneword[20];
    char c;
    int size = INITIAL_LEN;
    int counter = 0;
    char* prefix = argv[2];

    fp1 = fopen(argv[1],"r");
    char** ptr = (char **) calloc (size, sizeof(char*));
    printf("Allocated initial array of %d character pointers.\n", size);
    if (ptr == NULL) {
      fprintf(stderr, "ALLOCATION FAILED :-(\n");
      return 1;
    }
    
    do {
      c = fscanf(fp1,"%s",oneword); 
      
      if (c == EOF) {
        break;
      }
      if (counter == size) {
        size = size * 2;
        ptr = realloc (ptr, size * sizeof(char*));
        printf("Reallocated array of %d character pointers.\n", size);
        if (ptr == NULL) {
          fprintf(stderr, "REALLOCATION FAILED :-(\n");
          return 1;
        }
      }
      ptr[counter] = malloc(1024 * sizeof(char));
      if (ptr[counter] == NULL) {
          fprintf(stderr, "WORD ALLOCATION FAILED :-(\n");
          return 1;
      }
      strcpy(ptr[counter], oneword);
      counter++;
      
    } while (c != EOF);              

    fclose(fp1);        
    
    
    int i;
    for (i = 0; i < counter; i++) {
      if(startsWith(ptr[i], prefix)) {
         printf("%s\n", ptr[i]);
      }
    }
    
    for (i = 0; i <counter; i++){
		free(ptr[i]);
	}
    free(ptr);

    
    return 0;
}
