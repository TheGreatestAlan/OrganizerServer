import obsidian
import gkeep

def merge_array(merger, mergee):
    for item in merger:
        if item not in mergee:
            #print('appending ' + item)
            mergee.append(item)

def remove_from_array(remover, removee):
    for item in remover:
        if item in removee:
            removee.remove(item)
def find_common_items(checker, checkee):
    common = []
    for item in checker:
        if item in checkee:
            common.append(item)
    return common

googlekeep = gkeep.GKeep()

gactive, gchecked = googlekeep.get_todo()
oactive, ochecked = obsidian.get_obsidian_todo()

checked = find_common_items(gactive, ochecked)
googlekeep.check_todo_items(checked)
remove_from_array(checked, gactive)
merge_array(gactive, oactive)
obsidian.write_obsidian_todo(oactive, ochecked)