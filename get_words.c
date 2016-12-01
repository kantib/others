#include<stdio.h>
#include <string.h>
#include <stdlib.h>

void fun_2(){

    char a[] = "    Hello   world  how   are you    ? ";
    int i, j, k;
    char b[100][100];

    i  = 0;
    j = 0;
    do {
        while(a[i] == ' ' || a[i] == '\t' || a[i] == '\n') {
            i++;
        }

        if (a[i] == '\0')
           break;

        k = 0;

        while(a[i] != ' ' && a[i] != '\t' && a[i] != '\n' && a[i] != '\0') {
            b[j][k] = a[i];
            i++;
            k++;
        }
        b[j][k] = '\0';
        j++;
    } while (a[i] !=  '\0');

    for (i = 0; i  < j; i++)
        printf("%s\n", b[i]);

}


void fun_1(char a[]) {


	//char a[] = "          !5       fsdf            ";
	char b[100];
	int i=0;
	int len;
	int j=0;
	memset(b,'\0',100);
	//do {
	while(1){
		while(a[i] == ' ' || a[i] == '\t' || a[i] == '\n') {
			i++;
 		}
		if(a[i] == '\0'){
			b[j]= '\0';
			break;
		}
		while(a[i] !=  ' ' && a[i] != '\t' && a[i] != '\n' && a[i] != '\0') {
			if (i != 0) {				
				if (a[i-1] == ' ' || a[i-1] == '\t'){
					b[j] = ' ';
					j++;
					b[j] = a[i];
				} else {
					b[j] = a[i];				
				}
			} else {
				b[j] = a[i];
			}
			i++;
			j++;
		}
	}
	memset(a, '\0',strlen(a));
	i =0; int k;
	len = strlen(b);
	if(b[0] == ' '){
		for(k=1;k<len;k++){
			a[i] = b[k];
			i++;
		}
	} else {
		for(k=0;k<len;k++){
			a[i] = b[k];
			i++;
		}
	}
	/*len = strlen(b);
	printf("b = %s, len = %d\n",b,len);
	len = strlen(a);
	printf("a = %s, len = %d\n",a,len);*/

}


int main(){
	/*char line[] = "   this         is  my    line.   ";
	printf("line BEFORE :%s\n",line);
	fun_1(line);
	printf("line AFTER :%s\n",line);*/

	const char *p;
	char c = '9';
	p = &c;
	printf("value from atoi = %d \n",atoi(p));
	return 0;
}
