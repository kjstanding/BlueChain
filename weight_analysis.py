import sys
import json
import os
import re
import math

from keras.models import load_model

THRESHOLD = 4  # Constant threshold value


def compare_weights(model1, model2):
    """Compare weights of two models."""
    params1 = model1.get_weights()
    params2 = model2.get_weights()
    diff = [math.sqrt(sum((p1_i - p2_i)**2 for p1_i, p2_i in zip(p1, p2))) for p1, p2 in zip(params1, params2)]
    return sum(diff)


def extract_number(filename):
    """Extract numerical value from a filename."""
    match = re.search(r'\d+', filename)
    return int(match.group()) if match else -1


def weight_analysis(model_dir):
    """Analyze model weights of each training interval."""
    bad_iterations = []
    model_snapshots = sorted((filename for filename in os.listdir(model_dir) if re.match(r'^\d+$', filename)),
                             key=extract_number)

    for i in range(1, len(model_snapshots)):
        model1 = load_model(os.path.join(model_dir, model_snapshots[i - 1]))
        model2 = load_model(os.path.join(model_dir, model_snapshots[i]))

        weights_diff = compare_weights(model1, model2)
        if weights_diff > THRESHOLD:
            bad_iterations.append(i - 1)

    return bad_iterations


if __name__ == "__main__":
    input_string = sys.stdin.readline().strip()
    result = weight_analysis(input_string)
    print(json.dumps(result))
    exit(0)
