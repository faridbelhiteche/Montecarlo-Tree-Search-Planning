# -*- coding: utf-8 -*-
"""
Created on Thu Nov 10 17:29:52 2022

@author: Juliette
"""
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

#on lit le fichier csv
data = pd.read_csv('results.csv', on_bad_lines='skip')
data


#on crée un vecteur pour chaque courbeà tracer
times_MRW_logistics=[]
steps_MRW_logistics=[]
times_HSP_logistics=[]
steps_HSP_logistics=[]
problems=[]

#on ajoute les valeurs dans chaque vecteur
for i in data.index:
    if data.domain[i] == 'domain_logistics.pddl':
        times_MRW_logistics.append(data.MRW_time_spent[i])
        steps_MRW_logistics.append(data.MRW_plan_length[i])
        times_HSP_logistics.append(data.HSP_time_spent[i])
        steps_HSP_logistics.append(data.HSP_plan_length[i])
        problems.append(str(data.n_problem[i]))


#on tri les vecteur pour que la performance du planner HSP soit croissante
sortedTimes=sorted(zip(times_HSP_logistics, times_MRW_logistics, problems))
times_HSP_logistics = [x for x,_,_ in sortedTimes]
times_MRW_logistics = [x for _,x,_ in sortedTimes]
problems = [x for _,_,x in sortedTimes]

sortedSteps = sorted(zip(steps_HSP_logistics, steps_MRW_logistics, problems))
steps_HSP_logistics = [x for x,_,_ in sortedSteps]
steps_MRW_logistics = [x for _,x,_ in sortedSteps]
problems = [x for _,_,x in sortedSteps]


#On génère les 2 graph
plt.figure(1)
pltTimes = plt.xlabel("Problem number"), plt.ylabel("Resolution time"), plt.title("Resolution time of MRW and HSP planners in logisticsworld")
pltTimes = plt.plot(problems, times_MRW_logistics, label = "MRW"), plt.plot(problems, times_HSP_logistics, label = "HSP"), plt.legend()
#plt.savefig("graphRuntimeLogisitcs.png") #on enregistre l'image

plt.figure(2)
pltSteps = plt.xlabel("Problem number"), plt.ylabel("Number of steps to resolve"), plt.title("Number of steps needed by MRW and HSP planners to resolve problem with logistics")
pltSteps = plt.plot(problems, steps_MRW_logistics, label = "MRW"), plt.plot(problems, steps_HSP_logistics, label = "HSP")
#plt.savefig("graphNumberOfStepsLogistics.png") #on enregistre l'image

