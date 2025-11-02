#!/bin/bash

# Check if a number parameter is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <number_of_files>"
    exit 1
fi

# Check if the parameter is a valid number
if ! [[ "$1" =~ ^[0-9]+$ ]]; then
    echo "Error: Parameter must be a positive number"
    exit 1
fi

# Create the target/inbox directory if it doesn't exist
mkdir -p ./target/inbox

# Create n empty files with unique names using timestamp
n=$1
timestamp=$(date +%Y%m%d_%H%M%S)
for i in $(seq 1 "$n"); do
    touch "./target/inbox/file_${timestamp}_$i.txt"
done

echo "Successfully created $n files in ./target/inbox"
