#include <stdio.h>
#include <string.h>
// pointer contains the address of an objexct 
// it is possible to access the object "inderectly"
#define forever for(;;)
#define max(A,B) ((A) > (B) ? (A) : (B))
#define see(expr) printf(#expr " = %g\n", expr)

#define paste(a, b) a ## b
int main(){
  int x = 0;
  int z;
  // &x will give the address
  printf("adress of var: %p\n", &x );
  int *p;
  int *p2;

  // assign addresss to pointer
  int* pc;
  int c;
  c = 5;
  pc = &c;
  printf("C-- %d\n", c);
  printf("PC address-- %p\n", pc);
  printf("PC value-- %d\n", *pc);
  c = 1;
  printf("PC address after -- %p\n", pc);
  printf("PC value after -- %d\n", *pc);
  *pc = 45; // reassign value to C
  printf("PC address after ater -- %p\n", pc);
  printf("PC value after ater -- %d\n", *pc);
  printf("PC value after after -- %d\n", c);
  printf("Z value -- %d\n", z);
  char a[] = "jiad";
  for (int i = 0; i < strlen(a); i++){
    putchar(a[i]);
  }

  x = max(10, 20);
  printf("max - %d\n", x);
  see(1.0 / 1.0);
  int res = paste(10, 20);
  see(res);
  return 0;
}