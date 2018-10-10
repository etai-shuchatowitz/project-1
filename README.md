# project-1

To Run:

```bash
git clone https://github.com/etai-shuchatowitz/project-1 # --recursive flag is necessary for dependencies
mvn clean package
mvn exec:java -Dexec.mainClass="Main"
```

Or run from your IDE of choice

Upon running you will print out 

```
Confusion matrix
------------------------------------
2 2 2 
2 2 2 
4 4 4 
------------------------------------
```

And the corresponding precision, recall and fMeasure are
```
vals are: StatData{precision=0.3333333333333333, recall=0.25, fMeasure=0.28571428571428575}
vals are: StatData{precision=0.3333333333333333, recall=0.25, fMeasure=0.28571428571428575}
vals are: StatData{precision=0.3333333333333333, recall=0.5, fMeasure=0.4}
```

You can find the topics for each folder in 0.txt, 1.txt and 2.txt (corresponding to C1, C4, and C7 respectively)