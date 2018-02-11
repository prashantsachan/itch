import sys
import scipy, numpy, pandas, sklearn
def printLibVersions():
    print('Python:{}'.format(sys.version))
    print('SciPy:{}'.format(scipy.__version__))
    print('NumPy:{}'.format(numpy.__version__))
    print('Pandas:{}'.format(pandas.__version__))
    print('Scikit-learn:{}'.format(sklearn.__version__))
    
printLibVersions()
