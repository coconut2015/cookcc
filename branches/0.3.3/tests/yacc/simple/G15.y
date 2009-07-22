%{ 
/************************************************************/
/* P3.y - Yacc defn file. 
/* This program outputs a compiler for D# language.
/* @ Input: filename.ds
/* @ Output: filename.asm
/* @ 611 Project 3. Due 2/28/06. Xin Chen.  
/************************************************************/

#include <stdio.h>  
int yyparse(void);
int yylex(void);

#define INFILE_SUFFIX ".ds"   /* suffix of D# program file. */
#define OUTFILE_SUFFIX ".asm" /* suffix of output file. */
#define MAX_SYMBOL_LEN 20     /* max length of symbol used. */
#define MAX_FILENAME 20       /* max length of filename. */

typedef struct {
  char symbol[MAX_SYMBOL_LEN];
} yystype;
#define YYSTYPE yystype

char asmfilename[MAX_FILENAME]; /* name of output asm file. */
FILE * asmfp; /* file pointer to the output asm file. */
int do_count = 0;

#include "stack.h"
stack * do_stack; /* keep track of do label. */

%}

%token Number
%token Identifier
%token Input
%token Output
%token Main
%token End
%token Int
%token Do
%token To

%%

program : Main ';' declaration_list code_start statement_list End Main ';'
        ;  

declaration_list : declaration_list declaration 
                 | declaration
                 ;

declaration : Int identifier_list ';'
            ;

identifier_list : identifier_list ',' Identifier 
  { fprintf(asmfp, "%s%s\n", $3.symbol, " dw ?"); }
                | Identifier 
  { fprintf(asmfp, "%s%s\n", $1.symbol, " dw ?"); }
                ;

code_start : /* empty */ { fprintf(asmfp, "start:\n"); }
           ;

statement_list : statement
               | statement_list statement 
               ; 

statement : assignment_statement
          | input_statement
          | output_statement
          | do_statement
          | error ';'
          ;

assignment_statement : Identifier '=' expression ';' 
  { 
    fprintf(asmfp, "  ;%s = %s\n", $1.symbol, $3.symbol);
    fprintf(asmfp, "  %s%s%s\n", "mov ", $1.symbol, ", ax");
  }
                 ;

input_statement : Input Identifier ';' 
  { fprintf(asmfp, "  ;Input %s\n", $2.symbol); 
    fprintf(asmfp, "  push offset prompt\n"); 
    fprintf(asmfp, "  call prtstring\n");
    fprintf(asmfp, "  call getsinte\n");
    fprintf(asmfp, "  mov %s ax\n", $2.symbol);
  }
                ;

output_statement : Output expression ';'
  { 
    fprintf(asmfp, "  ;Output %s\n", $2.symbol);
    fprintf(asmfp, "  mov ax, %s\n", $2.symbol);
    fprintf(asmfp, "  push ax\n");
    fprintf(asmfp, "  call prtsint\n");
    fprintf(asmfp, "  push offset CRLF\n"); // print line break
    fprintf(asmfp, "  call prtstring\n");
  }
                 ;
  
do_statement : do_head statement_list End Do ';'
  {
    fprintf(asmfp, "  ;inc %s\n", $$.symbol);
    fprintf(asmfp, "  mov ax, %s\n", $$.symbol);
    fprintf(asmfp, "  add ax, 1\n");
    fprintf(asmfp, "  mov %s, ax\n", $$.symbol);
    fprintf(asmfp, "  jmp D%d\n", stack_top(do_stack));
    fprintf(asmfp, "E%d:\n", stack_top(do_stack));
    stack_pop(do_stack);
  }
             ;

do_head : Do Identifier '=' Number To Number ';'
  {
    fprintf(asmfp, "  ;Do %s = %s To %s\n", $2.symbol, $4.symbol, $6.symbol);
    //printf("Do %s = %s to %s\n", $2.symbol, $4.symbol, $6.symbol);
    if (strcmp($4.symbol, "ax") != 0) {
      fprintf(asmfp, "  mov ax %s\n", $4.symbol); // not expr, must be int???
    }
    fprintf(asmfp, "  mov %s ax\n", $2.symbol);
    strcpy($$.symbol, $2.symbol);
    do_count ++;
    stack_push(do_stack, do_count);
    fprintf(asmfp, "D%d:\n", do_count);
    fprintf(asmfp, "  mov ax %s\n", $2.symbol);
    fprintf(asmfp, "  cmp ax %s\n", $6.symbol);
    fprintf(asmfp, "  jg E%d\n", do_count);
  }
        ;

expression : expression '+' primary 
  {
    fprintf(asmfp, "  ;%s + %s\n", $1.symbol, $3.symbol);
    if (strcmp($1.symbol, "ax") != 0) {
      fprintf(asmfp, "  %s%s\n", "mov ax, ", $1.symbol);
      strcpy($$.symbol, "ax");
    }
    fprintf(asmfp, "  %s%s\n", "add ax, ", $3.symbol);
  }
	   | primary
	   ;

primary : Identifier  
        | Number       
        ;

%%

void write_header(FILE * fp) { // write asm file header.
  fprintf(fp, ".model tiny\n");
  fprintf(fp, ".586\n");
  fprintf(fp, ".external prtstring, getsinte, prtsint\n");
  fprintf(fp, "  jmp short start\n");
  fprintf(fp, "prompt db \"Enter a number: \", 0\n");
  fprintf(fp, "CRLF db 0DH, 0AH, 0\n");
}

void write_footer(FILE * fp) { // write asm file footer.
  fprintf(fp, ".exit\n");
  fprintf(fp, ".end\n");
}

/*********************************************************
 * Get asm file name from input file name.
 * @Input: infilename - char *. 
 *    infilename should end with INFILE_SUFFIX, 
 *    and its length should < MAX_FILENAME.
 *********************************************************/
int get_output_filename(char * infilename) {
  int len = strlen(infilename);
  int in_suf_len = strlen(INFILE_SUFFIX);
  if (strcmp(infilename + len - in_suf_len, INFILE_SUFFIX) == 0) {
    len -= in_suf_len;
  } else {
    printf("input file name should have suffix %s\n", INFILE_SUFFIX);
    return -1;
  }
  if (len >= MAX_FILENAME - strlen(OUTFILE_SUFFIX)) {
    printf("input file name should have less than %d characters.\n",
           in_suf_len);
    return -1;
  }
  strncpy(asmfilename, infilename, len);
  strcat(asmfilename, OUTFILE_SUFFIX);
  //printf("asm file name: %s\n", asmfilename);
  return 0;
}


/*********************************************************
 * Entry point of the program.
 *********************************************************/
int main(int argc, char * argv[]) {
  if (argc < 2 || argc > 3) {
    printf("usage: ./a.out input_file [-c]\n"); return 0;
  } 
  if (get_output_filename(argv[1]) < 0) { return 0; }
  int convert2dos = 0; // whether to convert output file to DOS format.
  if (argc == 3 && strcmp(argv[2], "-c") == 0) { convert2dos = 1; }

  extern FILE * yyin;
  if ((yyin = fopen(argv[1], "r")) == NULL) { // open input file.
    printf("Cannot open input file %s\n", argv[1]);
    return 0;
  }
  if (creat(asmfilename, 0666) == -1) { // clear asm output file.
    printf ("Cannot create output file %s\n", asmfilename);
    fclose(yyin);
    return 0; 
  }
  if ((asmfp = fopen(asmfilename, "a")) == NULL) { // open output file.
    printf("Cannot open output asm file %s\n", asmfilename);
    fclose(yyin);
    return 0; 
  }

  write_header(asmfp);
  do_stack = stack_create(10); // to keep track of do label.
  yyparse();
  stack_destroy(do_stack);
  write_footer(asmfp);

  fclose(asmfp);
  fclose(yyin);

  if (convert2dos == 1) {
    // convert the asm file to DOS format: use \r\n instead of \n.
    char cmd[2 * MAX_FILENAME + 10];
    sprintf(cmd, "unix2dos %s %s", asmfilename, asmfilename);
    system(cmd);
  }

  return 0;
}


