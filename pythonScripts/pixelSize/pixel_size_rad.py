# python3

from math import atan, atan2, fsum

# amount mm, amount pixels, length between cam and picture
measure: list[list[int]] = [
    # [5, 30, 100],  # 10-1
    [5, 29, 100],  # 10-2
    [5, 29, 100],   # 10cm
    [5, 18, 150],  # 15cm
    # [5, 17, 150],  # 15-1
    [5, 18, 150],  # 15-2
    [5, 13, 200],  # 20cm
    [5, 13, 200],  # 20-1
    [5, 14, 200]   # 20-2
]


def evaluate(measure: list[list[int]]) -> list[int]:
    res: list[int] = list()
    for item in measure:
        angle = atan2((item[0]/item[1]), item[2])
        res.append(angle)
    return res;


def average(intList: list[int]) -> int:
    size = len(intList)
    listSum = fsum(intList)
    return listSum/size;


def main():
    res = evaluate(measure)
    print(res)
    avg = average(res)
    print(avg)

if __name__ == '__main__':
    main()
