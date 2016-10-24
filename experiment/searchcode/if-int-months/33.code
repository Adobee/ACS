int n = daysAvailable.length;
double items = 0;
int months = 0;
for(int i=0;i<n;i++){
if(daysAvailable[i]!=0){
items += (sales[i]*30.0/daysAvailable[i]);
months ++;
}
}

return   (int)(Math.ceil(items*1.0/months-1e-9));
}
}

