import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

#on lit le fichier csv
data = pd.read_csv('results.csv', on_bad_lines='skip')
data


#on crée un vecteur pour chaque courbeà tracer
times_MRW=[]
steps_MRW=[]
times_HSP=[]
steps_HSP=[]
problems=[]

#on ajoute les valeurs dans chaque vecteur
for i in data.index:
    #if data.domain[i] == 'domain_depots.pddl':
    if data.domain[i] == 'domain_blocks.pddl':
        index = i
        times_MRW.append(data.MRW_time_spent[i])
        steps_MRW.append(data.MRW_plan_length[i])
        times_HSP.append(data.HSP_time_spent[i])
        steps_HSP.append(data.HSP_plan_length[i])
        problems.append(str(data.n_problem[i]))


#on tri les vecteur pour que la performance du planner HSP soit croissante
sortedTimes=sorted(zip(times_HSP, times_MRW, problems))
times_HSP = [x for x,_,_ in sortedTimes]
times_MRW = [x for _,x,_ in sortedTimes]
problems = [x for _,_,x in sortedTimes]

sortedSteps = sorted(zip(steps_HSP, steps_MRW, problems))
steps_HSP = [x for x,_,_ in sortedSteps]
steps_MRW = [x for _,x,_ in sortedSteps]
problems = [x for _,_,x in sortedSteps]


#On génère les 2 graph
plt.figure(1)
pltTimes = plt.xlabel("Problem number"), plt.ylabel("Resolution time"), plt.title("Resolution time of MRW and HSP planners with"+data.domain[index])
pltTimes = plt.plot(problems, times_MRW, label = "MRW"), plt.plot(problems, times_HSP, label = "HSP"), plt.legend()
plt.savefig("Time_"+data.domain[index]+".png") #on enregistre l'image

plt.figure(2)
pltSteps = plt.xlabel("Problem number"), plt.ylabel("Number of steps to resolve"), plt.title("Number of steps needed by MRW and HSP planners to resolve problem with "+data.domain[index])
pltSteps = plt.plot(problems, steps_MRW, label = "MRW"), plt.plot(problems, steps_HSP, label = "HSP"), plt.legend()
plt.savefig("Steps_"+data.domain[index]+".png") #on enregistre l'image

