from fastapi import FastAPI
from app.routers import charts

app = FastAPI(
    title="Chart API",
    version="1.0",
)

app.include_router(charts.router, prefix="/api")

@app.get("/")
async def root():
    return {"message": "Is working"}