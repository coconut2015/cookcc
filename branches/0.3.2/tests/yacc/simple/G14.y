%{ 
/************************************************************/
/* P2.y - Yacc defn file.                                   */
/* This program pinpoints syntax errors in a C source file. */
/* @ 611 Project 2. Due 2/16/06. Xin Chen.                  */
/************************************************************/

#include <stdio.h>  
#define YYSTYPE int
int yyparse(void);
int yylex(void);

char * outfilename = "major4.lst"; /* output file. */
char * infilename = "major4.bas";  /* default input file. */

#define MAX_LINE_LEN 100
#define MAX_ERR_COUNT 999
typedef struct {
  int row;
  int col;
} err_entry; /* Record row and col of error. */
err_entry err_table[MAX_ERR_COUNT];
int err_index = 0;

void print_output();
int get_int_len(int x); /* Get the length of an integer. */
void print_err_mark(int start, int end, FILE * outfile);
void print_err_table();  /* Print row and column of errors */
%}

%token number
%token identifier
%token Input
%token Output
%token If

%%

program : list_statement 
        ;  

list_statement : statement
               | list_statement statement 
               ; 

statement : assign_statement
          | input_statement
          | output_statement
          | if_statement
          | error ';'
          ;

assign_statement : identifier '=' expression ';' 
                 ;

input_statement : Input identifier ';' 
                ;

output_statement : Output expression ';'
                 ;

if_statement : If '(' expression '=' expression ')' '{' list_statement '}'
             | If '(' error ')' '{' list_statement '}'
             ;

expression : expression '+' term    
	   | term
	   ;

term : identifier  
     | number       
     ;

%%

/*********************************************************
 * Entry point of the program.
 *********************************************************/
int main(int argc, char * argv[]) {
  if (creat(outfilename, 0666) == -1) { // clear output file.
    printf ("Cannot create output file %s\n", outfilename); 
    return 0;
  }

  // Get input filename if given by cmd line argument.
  if (argc > 1) { infilename = argv[1]; }

  extern FILE * yyin;
  if ((yyin = fopen(infilename, "r")) == NULL) {
    printf("Cannot open input file %s\n", infilename);
    return 0;
  }
  yyparse();
  fclose(yyin);

  //print_err_table(); // print row and col of each error.
  print_output();

  return 0;
}


/*********************************************************
 * Prints error mark under the line that has error.
 * @Input: start - int. Current position of column pointer.
 * @Input: end - int. The column index of the error. 
 * @Input: outfile - FILE *. If outfile is NULL, use stdout.
 *********************************************************/
void print_err_mark(int start, int end, FILE * outfile) {
  if (outfile == NULL) {
    for (; start < end - 1; ++ start) { printf(" "); }
    printf("*");
  } else {
    for (; start < end - 1; ++ start) { fprintf(outfile, " "); }
    fprintf(outfile, "*");
  }
}


/*********************************************************
 * Gets the number of digits of a non-negative integer.
 * This is used to align the '*' under the error line.
 * @Input: x - int. A non-negative integer.
 *********************************************************/
int get_int_len(int x) {
  if (x == 0) return 1;
  int i = 0;
  while (x > 0) { x = x/10; i++; }
  return i;
}


/*********************************************************
 * Prints output to output file and stdout.
 *********************************************************/
void print_output() {
  FILE * infile;
  FILE * outfile;
  if ((outfile = fopen(outfilename, "a")) == NULL) {
    printf("Cannot open output file %s\n", outfilename);
    return;
  }
  if ((infile = fopen(infilename, "r")) == NULL) {
    printf("Cannot open input file %s\n", infilename);
    return;
  }

  int i = 1;
  int j = 0;
  int col_ptr, cycle;
  char line[MAX_LINE_LEN];
  while (fgets(line, MAX_LINE_LEN, infile) != NULL) {
    if (j <= err_index && i == err_table[j].row) { // Print error lines.
      printf("%d. %s", i, line);
      fprintf(outfile, "%d. %s", i, line);
      col_ptr = -1 * get_int_len(i) - 2; // Count line no in initial value. 
      cycle = 0; // Used to count number of cycles.
      while (i == err_table[j].row) { // Print error mark.
        print_err_mark(col_ptr, err_table[j].col, NULL);
        print_err_mark(col_ptr, err_table[j].col, outfile);
        col_ptr = err_table[j].col; // Update current column location.
        cycle ++; 
        j ++; // Go to next error.
      } // handle multiple statements on the same line.
      printf("\nsyntax error%s\n\n", cycle>1?"s":"");
      fprintf(outfile, "\nsyntax error%s\n\n", cycle>1?"s":"");
    } else { // Print correct lines (only to output file).
      fprintf(outfile, "%d. %s", i, line);
    }
    i ++;
  }

  if (err_index == 0) {
    printf("\n**No syntax error is found.\n");
    fprintf(outfile, "\n**No syntax error is found.\n");
  } else {
    char * s = (err_index == 1?"":"s");
    printf("\n**%d syntax error%s\n", err_index, s);
    fprintf(outfile, "\n**%d syntax error%s\n", err_index, s);
  }

  fclose(outfile);
  fclose(infile);
}


/*********************************************************
 * Prints error table: row and col info of each error.
 *********************************************************/
void print_err_table() {
  int i;
  printf("--Error Table--\n");
  for (i=0; i<err_index; i++) {
    printf("error %d: row=%d, col=%d\n", i+1, 
           err_table[i].row, err_table[i].col);
  }
}

