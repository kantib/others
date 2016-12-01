#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define true 1
#define false 0


struct node{
	int pid;
	char pname[20];
	struct node *next;
};

/*Function to add a node at the end of the list*/
void add_node(struct node** root,int f_pid, char f_pname[])
{
	struct node *temp;
	struct node *new_node;
	temp = *root;
	if (*root != NULL) {
		while(temp->next != 0) {
			temp = temp->next;
		}

		new_node = (struct node *)malloc(sizeof(struct node));
		new_node->pid = f_pid;
		strcpy(new_node->pname, f_pname);
		new_node->next = 0;
		temp->next = new_node;
		printf("adding done\n");

	} else {
		
		*root = (struct node *)malloc(sizeof(struct node));
		(*root)->pid = f_pid;
		strcpy((*root)->pname, f_pname);		
		(*root)->next = NULL;
		printf("adding done\n");

/*		if(*root != NULL)
			printf("root has a value\n");
		else
			printf("root is still null\n");*/
	}
}

/*Function to delete a node with pid = f_pid */
int delete_node(struct node **root, int f_pid)
{
	struct node *temp;
	struct node *temp1;
	int flag = false;

	if(*root == NULL){
		printf("no nodes in the list\n");
		return -1; //failure to delete any node
	} else {  //case where there exist one or more nodes
		if((*root)->next == NULL){  //Case where only one node exist in the list
			if((*root)->pid == f_pid) { //case where that one node pid match f_pid
				temp = *root;
				*root = NULL;  //assign root back to NULL and free the node.
				free(temp);
				printf("Node with pid ->[%d] deleted 1\n",f_pid);
				return 0;
			} else { //case where there exist one node and its pid does not match f_pid
				printf("no match found. returning to main()\n");
				return -1; //failure to delete any node.
			}
		} else { //Case where there exist more than one node in the list
			temp = *root;
			temp1 = *root;
			while(true) {
				if(temp->pid == f_pid){ //if match found
					flag = true;    
					if(temp->next == NULL) { //Case to handle match found in the last node
						temp1->next = NULL;
						free(temp);
						printf("Node with pid->[%d] deleted 2\n",f_pid); 
					}
					if(temp->next != NULL) { //case to handle where match found in the middle of the nodes
						if(temp == *root) {
							*root = (*root)->next;
						} else {
							temp1->next = temp->next;
						}
						free(temp);
						printf("Node with pid->[%d] deleted 3\n",f_pid);
					}
					return(0);
				}
				if(temp->pid != f_pid) {
					temp1 = temp;
					temp = temp->next;
				}
				if(temp == NULL){
					flag = false;
					return (-1);					
				}
			}
		}
	}	

}


/*function to search the list*/
int search_list(struct node **root, int f_pid)
{
	int return_val = false;	
	struct node *temp;
	temp = *root;
	while(temp !=0)
	{
		if(f_pid == temp->pid){
			return_val = true;
			break;
		} else {
			//printf("pid not matched with prev node. moving to next\n" );
			temp = temp->next;		
		}		
	}
	return return_val;
		
}


/* Function to print the list*/
void print_list(struct node **root)
{
	struct node *temp;
	temp = *root;
	printf("\nroot -> ");
        while(temp !=0)
        {
        	printf("|%d,%s| -> ",temp->pid,temp->pname);
        	temp = temp->next;
    	}
    	printf("NULL\n\n");
}

/*Function to delete the entire list*/
void delete_list(struct node **root)
{
	struct node *temp;
	temp = *root;
	while(1){
		if(*root != 0){
			temp = *root;
			*root=(*root)->next;
			free(temp);
			printf("Freed next node.\n");
		}else{
			printf("delete_list:list Empty\n");
			break;
		}
	}	
}

int main()
{
	struct node *root = NULL;
	int in_pid, i,ret_state, j = 0;
	char c;
	char in_pname[20];
	if(root == NULL)
	{
		printf("List Empty. Want to fill the List? (y/n):\n");
		c = getchar();
		while(c == 'y'|| c == 'Y'){
			printf("pid = ");
			scanf("%d\n",&in_pid);

			printf("pname = ");
			c = getchar(); i = 0;
			while(c != '\n'){
				in_pname[i] = c;
				i++;
				c = getchar();
			}
			in_pname[i] = '\0';
			add_node(&root,in_pid,in_pname);
			//printf("added %d th node in the list\n",++j);
			if (root != NULL){
				//printf("calling print_list\n");
				print_list(&root);
			}
			else{
				printf("ERROR...\n");
			}
			printf("want more nodes ? (y/n): ");
			c = getchar();
		}
		
		printf("Want to delete any node?(y/n):\n");
		char p;
		p = getchar();
		p = getchar();
		printf("\n");
		while(p == 'y'|| p == 'Y'){
			printf("Enter pid : ");
			scanf("%d", &in_pid);
			printf("\n");

			if(root == NULL){ 
				printf("list is empty. no process running in the back\n");
				break; 
			}
			ret_state = search_list(&root,in_pid);
			if(ret_state){
				//printf("==> Pid found\n");
				ret_state = delete_node(&root, in_pid);
				//printf("return state from delete_node = %d\n", ret_state);
				if (root != NULL){
					//printf("calling print_list\n");
					print_list(&root);
				}
				else{
					printf("Empty list.no nodes to print\n");
					break;
				}
			}else{
				printf("No such background process - [%d]\n", in_pid);
			}
			printf("Want to delete any node?(y/n):");
			p = getchar();			
			p = getchar();
		}
		//delete_list(&root);
	}
	printf("returning from main()\n");
	return 0;	
}


