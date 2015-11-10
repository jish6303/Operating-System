#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <limits.h>
#include <unistd.h>
#include <pthread.h>
#include <stdbool.h>

typedef struct {
    int low;
    int up;
    int* res;
    char oper;
    pthread_mutex_t* sync;
} thread_args;

bool change_first = false;

int calculation(int, int);

#define ARR_LEN 1024

bool validOp(char ch) {
    if(ch == '*' || ch == '/' || ch == '+' || ch == '-') {
        return true;
    }
    else return false;
}

char expression[ARR_LEN];

int sub_term(int index, int length) {
    int cur = index;
    if(expression[cur] != '(')
        return -1;
    int unmatch = 1;
    cur++;
    while((cur < length) && (unmatch > 0)) {
        if(expression[cur] == '(') {
            unmatch++; 
        }
        else if(expression[cur] == ')') {
            if(unmatch > 0)
                unmatch--;
            else{
                return -1;
            }
        }
        cur++;
    }
    if(unmatch != 0) {
        return -1;
    }
    return cur;
}

void indic(int temp, int length, int* index) {
    int arrayindex = 0;
    if(expression[temp] == '(') {
        index[arrayindex] = temp;
        temp++;
        arrayindex++;
    }
    else{
        fprintf(stderr,"Bad expression error\n");
        exit(EXIT_FAILURE);
    }
    while(temp <= length) {
        if(expression[temp] == '-' && isdigit(expression[temp + 1])) {
            index[arrayindex] = temp;
            arrayindex++;
            temp++;
            while(isdigit(expression[temp])) 
                temp++;
        }
        else if(isdigit(expression[temp])) {
            index[arrayindex] = temp;
            arrayindex++;
            while(isdigit(expression[temp])) 
                temp++;
        }
        else if(expression[temp] == '(') {
            index[arrayindex] = temp;
            temp = sub_term(temp, length);
            if(temp == -1) {
                fprintf(stderr,"Bad expression error\n");
                exit(EXIT_FAILURE);
            }
            arrayindex++;
        }
        else
            temp++;
    }
    index[arrayindex] = temp;
}

void makeVal(int* arr, unsigned int length, int value) {
    int temp = 0;
    while(temp < length) {
        arr[temp] = value;
        temp++;
    }
}

char getOp(int length, int* index) {
    int start = index[0];
    int finish = index[1];
    if(start == -1 || finish == -1) {

    }
    for(; start < finish; start++) {
        if(validOp(expression[start]))
            return expression[start];
    }
    return '\0';
}

void* tokenizer(void* args) {
    thread_args* argum = (thread_args*)args;
    char opr = argum->oper;
    int lo = argum->low;
    int up = argum->up;
    int* val = argum->res;
    pthread_mutex_t* mutex = argum->sync;
    int res = INT_MIN;
    if(expression[lo] == '-' && isdigit(expression[lo + 1])) {
        char numb[ARR_LEN];
        memset(numb, '\0', ARR_LEN);
        int cuindex = lo;
        int sizenum = 0;
        numb[sizenum] = expression[cuindex];
        sizenum++;
        cuindex++;
        while(isdigit(expression[cuindex])) {
            numb[sizenum] = expression[cuindex];
            sizenum++;
            cuindex++;
        }
        res = atoi(numb);
    }
    else if(isdigit(expression[lo])) {
        char numb[ARR_LEN];
        memset(numb, '\0', ARR_LEN);
        int cuindex = lo;
        int sizenum = 0;
        while(isdigit(expression[cuindex])) {
            numb[sizenum] = expression[cuindex];
            sizenum++;
            cuindex++;
        }
        res = atoi(numb);
    }
    else if(expression[lo] == '(') {
        res = calculation(lo, up);
    }
    pthread_mutex_lock(mutex);
    if(opr == '+') {
        if(change_first == true){
           change_first = false;
           printf("THREAD %u: Starting with first operand '%d'\n", (unsigned int)pthread_self(), res);
        } else{
           printf("THREAD %u: Adding '%d'\n", (unsigned int)pthread_self(), res);   
        }
        *val += res;
    }
    else if(opr == '/') {
        printf("THREAD %u: Dividing by '%d'\n", (unsigned int)pthread_self(), res);
        *val /= res;
    }
    else if(opr == '*') {
        if(change_first == true){
           change_first = false;
           printf("THREAD %u: Starting with first operand '%d'\n", (unsigned int)pthread_self(), res);
        } else{
            printf("THREAD %u: Multiplying by '%d'\n", (unsigned int)pthread_self(), res);   
        }
        *val *= res;
    }
    else if(opr == '-') {
        printf("THREAD %u: Subtracting '%d'\n", (unsigned int)pthread_self(), res);
        *val -= res;
    }
    pthread_mutex_unlock(mutex);
    pthread_exit(0);
    return EXIT_SUCCESS;
}

int calculation(int cur, int length) {
    int term = sub_term(cur, length);
    int index[term];
    makeVal(index, term, -1);
    indic(cur, term, index);
    int numOpt = 0;
    while(index[numOpt] != -1 && numOpt < term) {
        numOpt++;
    }
    if(numOpt < 3) {
        fprintf(stderr,"Not enough operands.\n");
        exit(EXIT_FAILURE);
        
    }
    char opr = getOp(term, index);
    if(opr == '\0') {
        char unknown_op[ARR_LEN];
        memset(unknown_op, '\0', ARR_LEN);
        int s = index[0] + 1;
        int t = index[1];
        while(isspace(expression[s])) {
            s++;
        }
        int itr = 0;
        while(s < t && !isspace(expression[s]))  {
            unknown_op[itr] = expression[s];
            s++; itr++;
        }
        fprintf(stderr,"ERROR: unknown '%s' operator\n", unknown_op);
        exit(EXIT_FAILURE);
    }
    printf("THREAD %u: Starting '%c' operation\n", (unsigned int)pthread_self(), opr);
    int i = 0;
    if(expression[index[i]] == '(') {
        i++;
    }
    pthread_t tid[numOpt];
    int threadNum = 0;
    int* res = (int*)malloc(sizeof(int));
    if(opr == '-' || opr == '+')
        *res = 0;
    else if(opr == '/' || opr == '*')
        *res = 1;
    char first = 't';
    pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
    for(; i < (numOpt - 1); i++) {
        thread_args* args = (thread_args*)malloc(sizeof(thread_args));
        if(opr == '-' && first == 't'){
            args->oper = '+';
            first = 'f';
            change_first = true;
        }
        else if(opr == '/' && first == 't'){
            args->oper = '*';
            first = 'f';
            change_first = true;
        }
        else args->oper = opr;
        pthread_t thread_id;
        args->low = index[i];
        args->up = index[i+1] - 1;
        args->res = res;
        args->sync = &mutex;
        pthread_create(&thread_id, NULL, tokenizer, args);
        if(opr == '/' && first == 't') {
            pthread_join(thread_id, NULL);
            first = 'f';
            continue;
        }
        else {
            tid[threadNum] = thread_id;
            threadNum++;
        }
    }
    int j = 0;
    for(; j < threadNum; j++) {
        pthread_join(tid[j], NULL);
    }
    printf("THREAD %u: Ended '%c' operation with result '%d'\n", (unsigned int)pthread_self(), opr, *res);
    return *res;
}

int main(int argc, char *argv[]) {
    if(argc > 2 || argc <2){
        fprintf(stderr, "ERROR: Invalid arguments\nUSAGE: ./a.out <input-file>\n");
		return EXIT_FAILURE;
	 }
    FILE* file = fopen(argv[1], "r");
    if(file == NULL) {
        fprintf(stderr,"ERROR: Can't find the file.\n");
        return EXIT_FAILURE;
    }
    memset(expression, '\0', ARR_LEN);
    int numCh = 0;
    char expreCh = getc(file);
    while((expreCh != '\n') && (expreCh != EOF)) {
        expression[numCh] = expreCh;
        numCh++;
        expreCh = getc(file);
    }
    fclose(file);
    int cur = 0;
    int length = strlen(expression);
    int res = calculation(cur, length);
    printf("THREAD %u: Final answer is '%d'\n", (unsigned int)pthread_self(), res);
    return EXIT_SUCCESS;
}
