import os
import obsidian

file_path = os.environ.get("TODO_LOCATION")
print(file_path)
array = obsidian.read_file_into_array(file_path+"new")
list_exists = False
while list_exists:
    if array.__len__() is 0:
        #sleep
        print("empty")
    else:
        list_exists = True




