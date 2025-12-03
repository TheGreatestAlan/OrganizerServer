import os

markdown_checked = '- [x]  '
markdown_unchecked = '- [ ]  '
def write_array_to_file(file_path, data_array):
    try:
        with open(file_path, 'w') as file:
            data_string = '\n'.join(data_array)
            file.write(data_string)
    except Exception as e:
        print(f"Error: An error occurred while writing to the file '{file_path}': {e}")


def read_file_into_array(file_path):
    try:
        with open(file_path, 'r') as file:
            lines = file.readlines()
            lines = [line.strip() for line in lines]
            return lines
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        return []
    except Exception as e:
        print(f"Error: An error occurred while reading the file '{file_path}': {e}")
        return []

def get_todo_list_items(todo_array):
    unchecked = []
    checked = []
    for todo in todo_array:
        if todo.startswith(markdown_unchecked):
            unchecked.append(todo.replace(markdown_unchecked, '', 1))
        if todo.startswith(markdown_checked):
            checked.append(todo.replace(markdown_checked, '', 1))
    return unchecked, checked

def get_obsidian_todo():

    file_path = os.environ.get("TODO_LOCATION")
    lines_array = read_file_into_array(file_path)
    return get_todo_list_items(lines_array)

def write_obsidian_todo(unchecked, checked):
    file_path = os.environ.get("TODO_LOCATION")
    try:
        with open(file_path, 'w') as file:
            data_string = '\n'.join(merge_into_checklist(unchecked, checked))
            file.write(data_string)
        print(f"Data successfully written to '{file_path}'.")
    except Exception as e:
        print(f"Error: An error occurred while writing to the file '{file_path}': {e}")

def merge_into_checklist(unchecked, checked):
    merged = []
    for item in unchecked:
        merged.append(markdown_unchecked + item)
    for item in checked:
        merged.append(markdown_checked + item)
    return merged
