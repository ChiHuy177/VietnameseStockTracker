FROM python:3.13-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    # vnstock/vnai are no longer published as files on PyPI (the project page is empty);
    # the official install docs point to the vendor's own index instead.
    PIP_EXTRA_INDEX_URL=https://vnstocks.com/api/simple

WORKDIR /app

COPY pyproject.toml README.md ./
COPY src ./src
RUN pip install .

EXPOSE 8000

# Port 8000 is the container-internal port (backend reaches it at http://python:8000);
# the port published on the host is configured in docker-compose.yml via PYTHON_PORT.
CMD ["uvicorn", "vst_python.api:app", "--host", "0.0.0.0", "--port", "8000"]
