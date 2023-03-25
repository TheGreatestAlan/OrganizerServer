import os
import subprocess

# replace 'cli_tool' with the name of your CLI tool
cli_tool_output = subprocess.run(['ado'], stdout=subprocess.PIPE)

# save the output of the CLI tool to a string variable
output_string = cli_tool_output.stdout.decode()

# print the output string
print("HELLO")