

#Скользящая средняя по 3 значениям
def moving_average_3(coll: list): 
	i = 0
	correct = list()
	correct.append(coll[0])
	while i < (len(coll)-2):
		i += 1
		correct.append((coll[i-1] + coll[i] + coll[i+1])/3)
	correct.append(0)
	return correct

