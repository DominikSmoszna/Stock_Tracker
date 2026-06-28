from fastapi import FastAPI
from app.routers import charts
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="Chart API",
    version="1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(charts.router, prefix="/api")

@app.get("/")
async def root():
    return {"message": "Is working"}