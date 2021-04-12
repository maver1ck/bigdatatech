from flask import Flask, render_template, request

import json
import os.path

DATA = "/data/history.txt"

app = Flask(__name__)


@app.route('/', methods=['GET', 'POST'])
def index():
    bmi = ''
    if request.method == 'POST':
        weight = float(request.form.get('weight'))
        height = float(request.form.get('height'))
        bmi = calc_bmi(weight, height)
        with open(DATA, "a") as f:
            f.write(json.dumps({'weight': weight, 'height': height, 'bmi': bmi}) + '\n')
    return render_template("bmi_calc.html", bmi=bmi)


@app.route('/history', methods=['GET'])
def history():
    history = []
    if os.path.isfile(DATA):
        with open(DATA, "r") as f:
            for line in f:
                history.append(json.loads(line))
    return render_template("history.html", history=history)


def calc_bmi(weight, height):
    return round((weight / ((height / 100) ** 2)), 2)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
