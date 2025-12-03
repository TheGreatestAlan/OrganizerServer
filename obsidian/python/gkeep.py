import os
import logging
import keyring
import gkeepapi
import sys


class GKeep:

    def find_cred_location(self, cred_filename):
        # Get the absolute path of the script
        script_path = os.path.abspath(__file__)
        # Extract the directory path
        script_dir = os.path.dirname(script_path)
        # Append the specified filename to the directory path
        file_path = os.path.join(script_dir, cred_filename)
        return file_path

    def read_first_two_lines(self, file_path):
        with open(file_path, 'r') as file:
            # Read the first two lines
            line1 = file.readline().strip()
            line2 = file.readline().strip()
            return line1, line2

    def login_gkeep(self):
        # Use an existing master token if one exists
        logged_in = False
        user, password = self.get_creds()
        token = keyring.get_password("google-self.keep-token", user)
        if token:
            try:
                self.keep.resume(user, token, sync=True)
                logged_in = True
                self.logger.info("Success")
            except gkeepapi.exception.LoginException:
                self.logger.info("Invalid token")
        # Otherwise, prompt for credentials and login
        if not logged_in:
            try:
                self.keep.login(user, password, sync=True)
                del password
                token = self.keep.getMasterToken()
                keyring.set_password("google-self.keep-token", user, token)
                self.logger.info("Success")
            except gkeepapi.exception.LoginException as e:
                self.logger.info(e)

    def get_creds(self):
        user = os.environ.get('KEEP_USER')
        password = os.environ.get('KEEP_PASS')
        file_path = self.find_cred_location('cred.cred')

        if user and password:
            print("using credentials from environment variables:")
        elif os.path.exists(file_path):
            user, password = self.read_first_two_lines(file_path)
            print("using credentials from 'creds' file:")
        else:
            print("error: credentials not found. set user and pass environment variables or create a 'creds' file.")
        return user, password

    def get_todo_list(self):
        gnotes = self.keep.all()
        for note in gnotes:
            if note.title == 'To Do':
                return note

    def get_todo(self):
        todo_active = []
        todo_done = []
        note = self.get_todo_list()
        todo_array = note.text.splitlines()
        for item in todo_array:
            if item.startswith(self.unchecked_checkbox):
                todo_active.append(item.replace(self.unchecked_checkbox, '', 1))
            if item.startswith(self.checked_checkbox):
                todo_done.append(item.replace(self.checked_checkbox, '', 1))

        return todo_active, todo_done

    def check_todo_items(self, items):
        note = self.get_todo_list()
        for todo in note.items:
            for item in items:
                if item in todo.text:
                    todo.checked = True
        self.keep.sync()

    def __init__(self):
        self.keep = gkeepapi.Keep()
        self.logger = logging.getLogger("gself.keepapi")

        self.login_gkeep()

        # Set up logging
        self.logger.setLevel(logging.INFO)
        ch = logging.StreamHandler(sys.stdout)
        formatter = logging.Formatter("[%(levelname)s] %(message)s")
        ch.setFormatter(formatter)
        self.logger.addHandler(ch)

        self.checked_checkbox = '☑ '
        self.unchecked_checkbox = '☐ '
