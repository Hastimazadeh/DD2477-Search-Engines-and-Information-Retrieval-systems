import matplotlib.pyplot as plt

# precision and recall values
precision = [0.6, 0.45, 0.433, 0.4, 0.38]
recall = [0.06, 0.09, 0.13, 0.16, 0.19]

# create line plot with bold points
plt.plot(recall, precision, 'o-')

# set axis labels and title
plt.xlabel('Recall')
plt.ylabel('Precision')
plt.title('Precision-Recall Line Graph with Bold Points')

# show plot
plt.show()