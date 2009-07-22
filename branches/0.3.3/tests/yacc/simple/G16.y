%{ 
/************************************************************
 * P4.y - Yacc defn file. 
 * This program outputs a compiler for D# language. 
 * It can output asm and obj files.
 * 
 * 2-pass parsing is used. 
 * Pass 1 builds symbol table. Pass 2 writes to output file.
 * 
 * @ Input: filename
 * @ Output: filename.asm, filename.obj
 * @ 611 Project 4. Due 5/2/06. Xin Chen.  
 ************************************************************/

#include "compiler.h"
#include "obj.h"      // functions for creating obj file.
#include <stdio.h>  
#include <stdlib.h>   // atoi.
int yyparse(void);
int yylex(void);

int PASS; // Number of pass in a 2-Pass parse. Value: 1 or 2.

#define EXPR_STACK_SIZE 1000
#define MAX_EXPR_COUNT 100
#define DATA_OFFSET_STACK_SIZE 200
#define MAX_NUMBER_SIZE 20 // used by function getStrVal().

// Keep track of expressions in OUTPUT statements,
// store all expressions in this buffer.
char expr_stack[EXPR_STACK_SIZE]; 
int expr_stacki = 0; // current index of expr_stack.
int expr[MAX_EXPR_COUNT]; // store index of each expression.
int expr_ct = 0; // used by pass 1 to count expr number.
int expr_ct2 = 0; // used by pass 2 to count expr number.

// Used to get offset.
enum OFFSET_TYPE {VAR, IN_PROMPT, OUT_PROMPT, RESULT, CRLF};
// offsets of vars and prompts.
int data_offset[DATA_OFFSET_STACK_SIZE]; 

#define ASMFILE_SUFFIX ".asm" // suffix of asm output file.
#define OBJFILE_SUFFIX ".obj" // suffix of object output file.

char objfilename[MAX_FILENAME]; // name of output obj file. 
FILE * objfp; // file pointer to the output obj file. 

// 0 - not create asm file, 1 - create asm file.
int CREATE_ASM = 0; 

char asmfilename[MAX_FILENAME]; // name of output asm file. 
FILE * asmfp; // file pointer to the output asm file. 

// function used to create asm file.
void asm_write_header(FILE * fp);
void asm_write_footer(FILE * fp);
void asm_print_vars(FILE * fp);
void asm_print_output_expressions(FILE * fp);
 
// for calling util.lib subroutines.
// The defined value relates to function extdef() in obj.c.
#define UTIL_GETDEC 1
#define UTIL_PUTDEC 2

int register_stack[] = {CX, BX, DI, SI};
int register_stacki = 4;
int reg;
int getReg();
void freeReg(int Register);
char * getRegStr(int reg);

// To keep track of if and while labels.
// Used to create ASM file only.
#include "stack.h"
int label_count = 0;
stack * label_stack;

%}

%token Identifier
%token Number
%token Input
%token Output
%token If
%token Eq
%token Ne
%token Le
%token While
%token Wend
%token Main
%token End
%token Int

%%

program : Main ';' declaration_list statement_list End Main ';'
        | Main ';' statement_list End Main ';'
        ;

declaration_list : declaration_list declaration | declaration 
                 ;

declaration : Int identifier_list ';' 
            ;

identifier_list : identifier_list ',' Identifier | Identifier
                ;

statement_list : statement_list statement | statement
               ;

statement : assign_statement 
          | input_statement | output_statement 
          | while_statement | if_statement
          | error 
          ;

while_statement : while_prefix statement_list Wend ';'
  {
    if (PASS == 2) {
      if (CREATE_ASM) {
        fprintf(asmfp, "\n; WHILE end\n");
        fprintf(asmfp, "jmp LABEL_%d_Head\n", 
                       stack_top(label_stack));
        fprintf(asmfp, "LABEL_%d_Tail:\n", stack_top(label_stack));
        stack_pop(label_stack);
      }
      // branch back, should be sign-extended
      obj_jmp($1.kind_of_location - (code_stacki + 3)); 
      // complete branch out instruction.
      code_stack[$1.location] = code_stacki - ($1.location + 2);
    }
  }
                ;

while_prefix : WHILE condition
  {
    if (PASS == 2) {
      // end of evaluating condition.
      if (CREATE_ASM) { } // do nothing
      // for obj
      $$.kind_of_location = $1.kind_of_location; // b4 cmp
      $$.location = $2.location; // start of IP_INC_lo in cond jmp 
    }
  }
             | WHILE error
             ;

WHILE : While
  {
    if (PASS == 2) {
      // begin of evaluating condition.
      if (CREATE_ASM) {
        label_count ++;
        stack_push(label_stack, label_count);
        fprintf(asmfp, "\n; while start\n");
        fprintf(asmfp, "LABEL_%d_Head:\n", stack_top(label_stack));
      }
      $$.kind_of_location = code_stacki;
    }
  }
      ;

condition : expression Eq expression
  {
    if (PASS == 2) {
      if (CREATE_ASM) {
        fprintf(asmfp, "\nCMP %s, %s\n", 
        getRegStr($1.location), getRegStr($3.location));
        fprintf(asmfp, "jne LABEL_%d_Tail\n", stack_top(label_stack));
      }
      obj_cmp_r2r($1.location, $3.location, D_TO);
      obj_jne_far(); // branch out instruction.
      $$.location = code_stacki - 2; // loc of IP_INCS_lo. 
      freeReg($1.location);
      freeReg($3.location);
    }
  }
          | expression Ne expression
  {
    if (PASS == 2) {
      if (CREATE_ASM) {
        fprintf(asmfp, "\nCMP %s, %s\n", 
        getRegStr($1.location), getRegStr($3.location));
        fprintf(asmfp, "je LABEL_%d_Tail\n", stack_top(label_stack));
      }
      obj_cmp_r2r($1.location, $3.location, D_TO);
      obj_je_far(); // branch out instruction.
      $$.location = code_stacki - 2; // loc of IP_INCS_lo.
      freeReg($1.location);
      freeReg($3.location);
    }
  }
          | expression Le expression
  {
    if (PASS == 2) {
      if (CREATE_ASM) {
        fprintf(asmfp, "\nCMP %s, %s\n", 
        getRegStr($1.location), getRegStr($3.location));
        fprintf(asmfp, "jg LABEL_%d_Tail\n", stack_top(label_stack));
      }
      obj_cmp_r2r($1.location, $3.location, D_TO);
      obj_jg_far(); // branch out instruction.
      $$.location = code_stacki - 2; // loc of IP_INCS_lo.
      freeReg($1.location);
      freeReg($3.location);
    }
  }
          ;

if_statement : if_prefix statement_list End If ';'
  {
    if (PASS == 2) {
      if (CREATE_ASM) {
        fprintf(asmfp, "\n; IF end\n");
        fprintf(asmfp, "LABEL_%d_Tail:\n", stack_top(label_stack));
        stack_pop(label_stack);
      }
      // complete branch out instruction.
      code_stack[$1.location] = code_stacki - ($1.location + 2);
    }
  }
             ;

if_prefix : IF condition
  {
    if (PASS == 2) {
      // end of evaluating condition.
      if (CREATE_ASM) { } // do nothing
      // for obj
      $$.location = $2.location; // start of IP_INC_lo in cond jmp
    }
  }
      | IF error
      ;

IF : If
  {
    if (PASS == 2) {
      // begin of evaluating condition.
      if (CREATE_ASM) {
        label_count ++;
        stack_push(label_stack, label_count);  
        fprintf(asmfp, "\n; IF start\n");
      }
    }
  }
   ;

assign_statement : Identifier '=' expression ';'
  { // Expression value is stored in SI.
    if (PASS == 2) {
      reg = $3.location;
      if (CREATE_ASM) { // for asm file.
        fprintf(asmfp, "\n; Assign %s to %s\n", 
                getRegStr($3.location), getSymbol($1.location));
        fprintf(asmfp, "MOV %s, %s\n", 
                getSymbol($1.location), getRegStr($3.location));
      }
      // for obj file.
      obj_mov(reg, DIRECT, 
              get_offset(getSymbol($1.location), 0, VAR),
              D_FROM); //from reg to mem.
      fix_offset();
      freeReg(reg);
    }
  }
                 ;

input_statement : Input Identifier  ';'
  {  
    if (PASS == 2) {
      if (CREATE_ASM) { // for asm file.
        fprintf(asmfp, "\n;Input %s\n", getSymbol($2.location));
        fprintf(asmfp, "MOV DX, OFFSET input_prompt_%s\n", 
                        getSymbol($2.location));
        fprintf(asmfp, "MOV AH, 9\n");
        fprintf(asmfp, "INT 21h ; display prompt\n");
        fprintf(asmfp, "CALL GetDec\n");
        fprintf(asmfp, "MOV %s, AX\n", getSymbol($2.location));
      }
      // for obj file.
      obj_mov_imm(DX, 
              get_offset(getSymbol($2.location), 0, IN_PROMPT));
      fix_offset();
      obj_mov_imm(AH, 9);
      obj_int21();
      obj_call_util();
      fix_offset_util(UTIL_GETDEC);
      obj_mov_acc_mem(1, AX, 
                  get_offset(getSymbol($2.location), 0, VAR));
      fix_offset();
    }
  }
                ;

output_statement : Output expression ';'
  { 
    if (PASS == 2) {
      reg = $2.location;
      if (CREATE_ASM) { // for asm file.
        fprintf(asmfp, "\n; Output %s\n", 
                expr_stack + expr[expr_ct2]);
        //fprintf(asmfp, "LEA DX, output_prompt\n");
        fprintf(asmfp, "MOV DX, OFFSET output_prompt_%d\n", 
                expr_ct2 + 1);
        fprintf(asmfp, "MOV AH, 9\n");
        fprintf(asmfp, "INT 21h ; display prompt\n");
        fprintf(asmfp, "MOV AX, %s\n", getRegStr($2.location));
        fprintf(asmfp, "CALL PutDec\n");
        fprintf(asmfp, "MOV DX, OFFSET CRLF\n");
        fprintf(asmfp, "MOV AH, 9\n");
        fprintf(asmfp, "INT 21h ; print new line\n");
      }
      // for obj file.
      obj_mov_imm(DX, get_offset(NULL, expr_ct2, OUT_PROMPT));
      fix_offset();
      obj_mov_imm(AH, 9);
      obj_int21();
      obj_mov(AX, reg, 0, D_TO); // move reg to AX.
      obj_call_util();
      fix_offset_util(UTIL_PUTDEC);
      obj_mov_imm(DX, get_offset(NULL, 0, CRLF));
      fix_offset();
      obj_mov_imm(AH, 9);
      obj_int21();
      expr_ct2 ++;
      freeReg(reg);
    } else if (PASS == 1) {
      // store this expression in buffer expr.
      expr[expr_ct] = expr_stacki;
      expr_ct ++;
      expr_stacki += (strlen(expr_stack + expr_stacki) + 1);
    }
  }
                 ;

expression : expression '+' term
  { 
    if (PASS == 2) {
      reg = $1.location;
      $$.kind_of_location = REGISTER;
      $$.location = reg;

      if (CREATE_ASM) { // for asm file.
        if ($3.kind_of_location == SYMREF) {
          fprintf(asmfp, "\n; add %s to %s\n", 
                  getSymbol($3.location), getRegStr(reg));
          fprintf(asmfp, "MOV AX, %s\n", getSymbol($3.location));
          fprintf(asmfp, "ADD %s, AX\n", getRegStr(reg));
        } else if ($3.kind_of_location == INTEGER) {
          fprintf(asmfp, "\n; add %d to %s\n", 
                  $3.location, getRegStr(reg));
          fprintf(asmfp, "MOV AX, %d\n", $3.location);
          fprintf(asmfp, "ADD %s, AX\n", getRegStr(reg));
        } else if ($3.kind_of_location == REGISTER) {
          fprintf(asmfp, "\n; add %s to %s\n", 
                  getRegStr($3.location), getRegStr(reg));
          fprintf(asmfp, "ADD %s, %s\n", 
                  getRegStr(reg), getRegStr($3.location));
        }
      }
      // for obj file.
      if ($3.kind_of_location == SYMREF) {
        obj_mov_acc_mem(0, AX, 
                        get_offset(getSymbol($3.location), 0, VAR)); 
        fix_offset();
        obj_add(reg, AX, 0); // add ax to reg
      } else if ($3.kind_of_location == INTEGER) {
        obj_mov_imm(AX, $3.location); // move term to ax.
        obj_add(reg, AX, 0); // add ax to reg
      } else if ($3.kind_of_location == REGISTER) {
        obj_add(reg, $3.location, 0); // add term to reg
        freeReg($3.location);
      }
    } else if (PASS == 1) {
      strcat(expr_stack + expr_stacki, " + ");
      strcat(expr_stack + expr_stacki, getStrVal($3));
    }
  }
           | expression '-' term
  { 
    if (PASS == 2) {
      reg = $1.location;
      $$.kind_of_location = REGISTER;
      $$.location = reg;

      if (CREATE_ASM) { // for asm file.
        if ($3.kind_of_location == SYMREF) {
          fprintf(asmfp, "\n; add %s to %s\n", 
                  getSymbol($3.location), getRegStr(reg));
          fprintf(asmfp, "MOV AX, %s\n", getSymbol($3.location));
          fprintf(asmfp, "SUB %s, AX\n", getRegStr(reg));
        } else if ($3.kind_of_location == INTEGER) {
          fprintf(asmfp, "\n; add %d to %s\n", 
                         $3.location, getRegStr(reg));
          fprintf(asmfp, "MOV AX, %d\n", $3.location);
          fprintf(asmfp, "SUB %s, AX\n", getRegStr(reg));
        } else if ($3.kind_of_location == REGISTER) {
          fprintf(asmfp, "\n; add %s to %s\n",
                  getRegStr($3.location), getRegStr(reg));
          fprintf(asmfp, "SUB %s, %s\n",
                  getRegStr(reg), getRegStr($3.location));
        }
      }
      // for obj file.
      if ($3.kind_of_location == SYMREF) {
        obj_mov_acc_mem(0, AX, 
                        get_offset(getSymbol($3.location), 0, VAR));
        fix_offset();
        obj_sub(reg, AX, 0); // sub ax from reg.
      } else if ($3.kind_of_location == INTEGER) {
        obj_mov_imm(AX, $3.location); // move term to ax.
        obj_sub(reg, AX, 0); // sub AX from reg.
      } else if ($3.kind_of_location == REGISTER) {
        obj_sub(reg, $3.location, 0); // sub term from reg.
        freeReg($3.location);
      }
    } else if (PASS == 1) {
      strcat(expr_stack + expr_stacki, " - ");
      strcat(expr_stack + expr_stacki, getStrVal($3));
    }
  }
           | term
  { // Need to move this term to a register.
    if (PASS == 2) {
      if ($1.kind_of_location == SYMREF || 
          $1.kind_of_location == INTEGER) { 
        reg = getReg(); 
        $$.kind_of_location = REGISTER;
        $$.location = reg;
      }
      else if ($1.kind_of_location == REGISTER) { 
        $$ = $1; reg = $1.location; }

      if (CREATE_ASM) { // for asm file. move to ax, then to a reg.
        if ($1.kind_of_location == SYMREF) {
          fprintf(asmfp, "\n; mov %s to %s\n", 
                         getSymbol($1.location), getRegStr(reg));
          fprintf(asmfp, "MOV AX, %s\n", getSymbol($1.location));
          fprintf(asmfp, "MOV %s, AX\n", getRegStr(reg));
        } else if ($1.kind_of_location == INTEGER) {
          fprintf(asmfp, "\n; mov %d to %s\n", 
                         $1.location, getRegStr(reg));
          fprintf(asmfp, "MOV AX, %d\n", $1.location);
          fprintf(asmfp, "MOV %s, AX\n", getRegStr(reg));
        } else if ($1.kind_of_location == REGISTER) {
          // do nothing.
        }
      }
      // for obj file.
      if ($1.kind_of_location == SYMREF) {
        obj_mov_acc_mem(0, AX, 
                        get_offset(getSymbol($1.location), 0, VAR));
        fix_offset();
        obj_mov(AX, reg, 0, D_FROM); // move AX to reg.
      } else if ($1.kind_of_location == INTEGER) {
        obj_mov_imm(AX, $1.location); // move term to ax.
        obj_mov(AX, reg, 0, D_FROM); // move AX to reg.
      } else if ($1.kind_of_location == REGISTER) {
        // do nothing.
      }
    } else if (PASS == 1) {
      strcpy(expr_stack + expr_stacki, getStrVal($1));
    }
  }
           ;

term : Identifier
        | Number
        | '(' expression ')' { $$ = $2; } 
        ;

%%


//////////////////////////////////////////////////
// Get a register as defined in register_stack.
//////////////////////////////////////////////////
int getReg() {
  -- register_stacki;
  if (register_stacki < 0) {
    printf("getReg error: underflow\n");
    return -1;
  }
  return register_stack[register_stacki];
}


//////////////////////////////////////////////////
// Free a register as defined in register_stack.
//////////////////////////////////////////////////
void freeReg(int Register) {
  static int REGISTER_STACK_SIZE = 
             sizeof(register_stack)/sizeof(int);
  //printf("size of register_stack: %d\n", REGISTER_STACK_SIZE);
  if (register_stacki >= REGISTER_STACK_SIZE) {
    printf("freeReg error: overflow\n");
    return;
  }
  register_stack[register_stacki] = Register;
  ++ register_stacki; 
}


//////////////////////////////////////////////////
// Given the integer value of a register,
// return a string standing for this register.
//////////////////////////////////////////////////
char * getRegStr(int reg) {
  if (reg == SI) return "SI";
  else if (reg == DI) return "DI";
  else if (reg == BX) return "BX";
  else if (reg == CX) return "CX";
  else { 
    printf("getRegStr error: Unknown reg type - %d\n", reg);
    return NULL;
  }
}


//////////////////////////////////////////////////
// Given the symbol table index of an identifier,
// return a pointer to this identifier string.
//////////////////////////////////////////////////
char * getSymbol(int symbol_index) {
  return identifier_list[
           symbol_table[symbol_index].identifier_index];
}


//////////////////////////////////////////////////
// Given a yylval type variable, return a string 
// for its value. This is most useful when
// yylval.kind_of_location = INTEGER.
//////////////////////////////////////////////////
char * getStrVal(YYSTYPE yylval) {
  static char int_str[MAX_NUMBER_SIZE];
  if (yylval.kind_of_location == SYMREF) {
    return getSymbol(yylval.location);
  } else if (yylval.kind_of_location == INTEGER) {
    sprintf(int_str, "%d", yylval.location);
    return int_str;
  } else if (yylval.kind_of_location == REGISTER) {
    return getRegStr(yylval.location);
  }
}


//////////////////////////////////////////////////
// symbol is used by VAR and IN_PROMPT,
// index is used by OUT_PROMPT,
// RESULT type does not use index.
//////////////////////////////////////////////////
int get_offset(char * symbol, int index, enum OFFSET_TYPE type) {
  if (type == VAR) {
    return data_offset[
           symbol_table[find(symbol, -1)].identifier_index]; 
  } else if (type == IN_PROMPT) {
    return data_offset[
           symbol_table[find(symbol, -1)].identifier_index +
                        identifier_list_index];
  } else if (type == OUT_PROMPT) {
    return data_offset[index + 2 * identifier_list_index];
  } else if (type == RESULT) {
    return data_offset[expr_ct + 2 * identifier_list_index];
  } else if (type == CRLF) {
    return data_offset[expr_ct + 2 * identifier_list_index + 1];
  }
  printf("get_offset error: unknown type\n");
  return 0;
}


//////////////////////////////////////////////////
// Print to the object file the variables
// in the data segment buffer data_stack.
//////////////////////////////////////////////////
void obj_print_vars(FILE * fp) {
  int i;
  // for variables.
  for (i = 0; i < identifier_list_index; i ++) { 
    data_offset[i] = data_stacki;
    sprintf(data_stack + data_stacki, "%c%c", 0, 0);
    data_stacki += 2;
    check_data_stack_size();
  }
  // for input prompts.
  for (i = 0; i < identifier_list_index; i ++) {
    data_offset[i + identifier_list_index] = data_stacki;
    sprintf(data_stack + data_stacki, "%s%s%s", 
            "Enter the value for ",
            identifier_list[i], ": $");
    data_stacki += 23 + strlen(identifier_list[i]);
    check_data_stack_size();
  }
  // for output prompts 
  for (i = 0; i < expr_ct; i ++) {
    data_offset[i + 2 * identifier_list_index] = data_stacki;
    sprintf(data_stack + data_stacki, "%s%s", 
            expr_stack + expr[i], ": The answer is: $");
    data_stacki += 18 + strlen(expr_stack + expr[i]);
    check_data_stack_size();
  }
  // for RESULT DW ?
  data_offset[expr_ct + 2 * identifier_list_index] = data_stacki;
  sprintf(data_stack + data_stacki, "%c%c", 0, 0);
  data_stacki += 2;
  check_data_stack_size();

  // for CRLF DB ?, 0Ah, 0Dh, "$"
  data_offset[expr_ct + 2 * identifier_list_index + 1] 
              = data_stacki;
  sprintf(data_stack + data_stacki, "%c%c%s", 0x0A, 0x0D, "$");
  data_stacki += 3;
  check_data_stack_size();

  //printf("offset array: %d items\n", 
  //  expr_ct + 2 * identifier_list_index);
  //for (i = 0; i <= expr_ct + 2 * identifier_list_index + 1; 
  //     i ++) {
  //  printf("offset %d: %d\n", i, data_offset[i]);
  //}
}


//////////////////////////////////////////////////
// First pass. Check error and build symbol table.
// return value: 0 is success. -1 is fail.
//////////////////////////////////////////////////
int pass_one(char * srcfile) {
  extern FILE * yyin;
  if ((yyin = fopen(srcfile, "r")) == NULL) { // open input 
    printf("Cannot open input file %s\n", srcfile);
    return -1;
  }

  // First pass, check error and build symbol table.
  PASS = 1;
  yyparse(); 
  fclose(yyin);

#ifdef DEBUG
  create_sorted_identifier_list();
  print_cross_ref_list();
  print_lineno_table();
  print_identifier_list(); // prints unsorted identifier list.
  print_symbol_table();
#endif

  if (SYNTAX_ERROR_FOUND) { return -1; }
  return 0; 
}


//////////////////////////////////////////////////
// Create and open output file(s).
//////////////////////////////////////////////////
int prepare_outfile() {
  if (creat(objfilename, 0666) == -1) { 
    printf ("Cannot create output obj file %s\n", objfilename);
    return -1;
  }
  if ((objfp = fopen(objfilename, "a")) == NULL) { 
    printf("Cannot open output obj file %s\n", objfilename);
    return -1;
  }

  if (CREATE_ASM) {
    if (creat(asmfilename, 0666) == -1) { 
      printf ("Cannot create output asm file %s\n", asmfilename);
      fclose(objfp);
      return -1;
    }
    if ((asmfp = fopen(asmfilename, "a")) == NULL) { 
      printf("Cannot open output asm file %s\n", asmfilename);
      fclose(objfp);
      return -1;
    }
  }
  return 0;
}


//////////////////////////////////////////////////
// Second pass. Create generated file(s).
//////////////////////////////////////////////////
int pass_two(char * srcfile) {
  extern FILE * yyin;
  if ((yyin = fopen(srcfile, "r")) == NULL) { // open input. 
    printf("Cannot open input file %s\n", srcfile);
    return -1;
  }

  if (prepare_outfile() == -1) { fclose(yyin); return -1; }

  if (CREATE_ASM) { 
    asm_write_header(asmfp); 
    label_stack = stack_create();  
  }
  obj_write_header(objfp, objfilename);

  // Second pass, write to output file.
  PASS = 2;
  yyparse();

  if (CREATE_ASM) { 
    asm_write_footer(asmfp); 
    fclose(asmfp); 
    stack_destroy(label_stack); 
  }
  obj_write_footer(objfp); 
  fclose(objfp);

  fclose(yyin);
  return 0;
}


/////////////////////////////////////////////////
// Get output file name(s).
/////////////////////////////////////////////////
void get_output_filename(char * infilename) {
  if (CREATE_ASM) {
    strcpy(asmfilename, infilename);
    strcat(asmfilename, ASMFILE_SUFFIX);
  }
  strcpy(objfilename, infilename);
  strcat(objfilename, OBJFILE_SUFFIX);
}


/////////////////////////////////////////////////
//  Entry point of the program.
/////////////////////////////////////////////////
int main(int argc, char * argv[]) {
  // initialize symbol_table and lineno_table.
  int i;
  for (i = 0; i < SYMBOL_TABLE_SIZE; ++ i) {
    symbol_table[i].hash_link = -1;
    symbol_table[i].lineno_index = -1;
    symbol_table[i].identifier_index = -1; 
  }
  for (i = 0; i < LINENO_TABLE_SIZE; ++ i) {
    lineno_table[i].lineno = -1;
    lineno_table[i].lineno_link = -1;     
  }

  if (argc < 2 || argc > 3) {
    printf("usage: ./Dsharp input_file [-a]\n"); 
    printf("use -a switch to output both obj and asm files.\n");
    return -1; 
  }

  if (argc == 3 && strcmp(argv[2], "-a") == 0) { CREATE_ASM = 1; }
  get_output_filename(argv[1]);

  if (pass_one(argv[1]) == -1) return -1; // first pass
  if (pass_two(argv[1]) == -1) return -1; // second pass

  if (CREATE_ASM) { 
    printf("%s and %s generated\n", objfilename, asmfilename); 
  } else { 
    printf("%s generated\n", objfilename);  
  }

  return 0;
}


//////////////////////////////////////////////////
// Functions used to generate asm file.
//////////////////////////////////////////////////


//////////////////////////////////////////////////
// Write the header of asm file.
//////////////////////////////////////////////////
void asm_write_header(FILE * fp) { // write asm file header.
  fprintf(fp, "extrn getdec : near, putdec : near\n");
  fprintf(fp, ".model small\n");
  fprintf(fp, ".stack\n");
  fprintf(fp, ".data\n");
  asm_print_vars(asmfp); // write variables in data segment.
}


//////////////////////////////////////////////////
// Write the footer of asm file.
//////////////////////////////////////////////////
void asm_write_footer(FILE * fp) { // write asm file footer.
  fprintf(fp, "\n;exit\n");
  fprintf(fp, "MOV AX, 4C00h\n");
  fprintf(fp, "INT 21h\n");
  fprintf(fp, "\nmain endp\n");
  fprintf(fp, "end main\n");
}


//////////////////////////////////////////////////
// Similar to print_identifier_list(), 
// print variables in data segment to asm file.
//////////////////////////////////////////////////
void asm_print_vars(FILE * fp) {
  int i;
  //printf("--variable list--\n");
  for (i = 0; i < identifier_list_index; i ++) {
    //printf("%d. %s\n", i, identifier_list[i]);
    fprintf(fp, "%s DW ?\n", identifier_list[i]);
  }
  for (i = 0; i < identifier_list_index; i ++) {
    fprintf(fp,
        "input_prompt_%s DB \"Enter the value for %s: $\"\n",
        identifier_list[i], identifier_list[i]);
  }
  asm_print_output_expressions(fp);
  fprintf(fp, "RESULT DW ?\n");
  fprintf(fp, "CRLF DB 0Ah, 0Dh, \"$\"\n");
  fprintf(fp, ".code\n");
  fprintf(fp, "main proc\n\n");
  fprintf(fp, "MOV AX, @DATA\n");
  fprintf(fp, "MOV DS, AX\n");
}


//////////////////////////////////////////////////
// Print output prompts in data segment to asm file. 
//////////////////////////////////////////////////
void asm_print_output_expressions(FILE * fp) {
  int i;
  //printf("--output expression list--\n");
  //printf("number of output expressions: %d, \n", expr_ct);
  //printf("used len of expr_stack(end 0 included): %d\n",
  //         expr_i);
  for (i = 0; i < expr_ct; i ++) {
    //printf("expr %d: %s\n", i + 1, expr_stack + expr[i]);
    fprintf(fp, 
      "output_prompt_%d DB \"%s: The answer is: $\"\n",
      i + 1, expr_stack + expr[i]);
  }
}


