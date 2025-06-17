from flask import Flask, render_template, request, redirect, url_for

app = Flask(__name__)
tasks = []

@app.route("/")
def index():
    return render_template("index.html", tasks=tasks)

@app.route("/add", methods=["POST"])
def add():
    task_content = request.form.get("task")
    if task_content:
        tasks.append({"content": task_content, "done": False})
    return redirect(url_for("index"))

@app.route("/complete/<int:task_id>")
def complete(task_id):
    if 0 <= task_id < len(tasks):
        tasks[task_id]["done"] = not tasks[task_id]["done"]
    return redirect(url_for("index"))

@app.route("/delete/<int:task_id>")
def delete(task_id):
    if 0 <= task_id < len(tasks):
        tasks.pop(task_id)
    return redirect(url_for("index"))

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
