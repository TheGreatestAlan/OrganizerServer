import os
import logging
import keyring
import getpass
import gkeepapi
import sys

def find_cred_location(cred_filename):
    # Get the absolute path of the script
    script_path = os.path.abspath(__file__)
    # Extract the directory path
    script_dir = os.path.dirname(script_path)
    # Append the specified filename to the directory path
    file_path = os.path.join(script_dir, cred_filename)
    return file_path

def read_first_two_lines(file_path):
	with open(file_path, 'r') as file:
		# Read the first two lines
		line1 = file.readline().strip()
		line2 = file.readline().strip()
		return line1, line2

def login_gkeep(keep):
    # Use an existing master token if one exists
    logged_in = False
    user, password = get_creds()
    token = keyring.get_password("google-keep-token", user)
    if token:
        logger.info("Authenticating with token")
        try:
            keep.resume(user, token, sync=True)
            logged_in = True
            logger.info("Success")
        except gkeepapi.exception.LoginException:
            logger.info("Invalid token")
    # Otherwise, prompt for credentials and login
    if not logged_in:
        try:
            keep.login(user, password, sync=True)
            logged_in = True
            del password
            token = keep.getMasterToken()
            keyring.set_password("google-keep-token", user, token)
            logger.info("Success")
        except gkeepapi.exception.LoginException as e:
            logger.info(e)

def get_creds():
    user = os.environ.get('KEEP_USER')
    password = os.environ.get('KEEP_PASS')
    file_path = find_cred_location('creds')

    if user and password:
        print("using credentials from environment variables:")
    elif os.path.exists(file_path):
        user, password= read_first_two_lines(file_path)
        print("using credentials from 'creds' file:")
    else:
        print("error: credentials not found. set user and pass environment variables or create a 'creds' file.")
    return user, password





# Set up logging
logger = logging.getLogger("gkeepapi")
logger.setLevel(logging.INFO)
ch = logging.StreamHandler(sys.stdout)
formatter = logging.Formatter("[%(levelname)s] %(message)s")
ch.setFormatter(formatter)
logger.addHandler(ch)

checked_checkbox = '☑'
unchecked_checkbox = '☐'


keep = gkeepapi.Keep()

login_gkeep(keep)

gnotes = keep.all()

todo_active = []
todo_done = []
for note in gnotes:
    if note.title == 'To Do':
        todo_array = note.text.splitlines()
        for item in todo_array:
            if item.startswith(unchecked_checkbox):
                todo_active.append(item)
            if item.startswith(checked_checkbox):
                todo_done.append(item)

logger.info(todo_done)
logger.info(todo_active)
